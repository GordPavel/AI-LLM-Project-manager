package com.tbank.aihelper.telegrambot.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

import com.tbank.aihelper.llm.LLMAdapter;
import com.tbank.aihelper.telegrambot.ChatBotAdapter;
import com.tbank.aihelper.telegrambot.component.PingJob;
import com.tbank.aihelper.telegrambot.dto.BotMessage;
import com.tbank.aihelper.telegrambot.dto.ParsedSetPingMessage;
import com.tbank.aihelper.telegrambot.dto.SetPingDto;
import com.tbank.aihelper.telegrambot.dto.UpdateContext;
import com.tbank.aihelper.telegrambot.entity.ChatConfiguration;
import com.tbank.aihelper.telegrambot.entity.JobBindingChat;
import com.tbank.aihelper.telegrambot.observer.EventListenerChatBot;
import com.tbank.aihelper.telegrambot.observer.ObserverChatBotAdapter;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
    /set_ping 'task_id: String' 'YYYY-MM-DDTHH:MM:SS(time_ping): ZonedDateTime' '[@username1, @username2, ...]: List<String>'
    /delete_ping 'ping_id: String'
*/
@Slf4j
@Service
@AllArgsConstructor
public class ScheduledPingService implements EventListenerChatBot {

    private final Scheduler scheduler;
    private final ChatBotAdapter chatBotAdapter;
    private final JobBindingService jobBindingService;
    private final ObserverChatBotAdapter observerChatBotAdapter;
    private final ConfigurateBotService configurateBotService;
    private final LLMAdapter llmAdapter;

    @PostConstruct
    private void subscribeToObserver() {
        observerChatBotAdapter.subscribe(this);
    }

    public void handleNewMessage(UpdateContext updateContext) {
        if(updateContext.getCommand() == null)
            return;

        if(updateContext.getTextMessage() == null 
            || updateContext.getTextMessage().isEmpty())
            return;

        switch (updateContext.getCommand()) {
            case "/set_ping":
                setPing(updateContext);
                break;
            case "/delete_ping":
                deletePing(updateContext);
                break;
            default:
                break;
        }
    } 

    private ParsedSetPingMessage parseSetPingMessage(
        String message,
        Long chatId,
        Integer messageId
    ) {
        List<String> words = Arrays.asList(message.split(" "));
        return ParsedSetPingMessage.builder()
                .setPingDto(SetPingDto.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .taskId(words.get(0))
                        .usernamesToPing(words.stream().skip(2)
                                .filter(word -> word.startsWith("@"))
                            .toList())
                    .build())
                .zonedDateTime(ZonedDateTime.of(
                    LocalDateTime.parse(words.get(1)), ZoneId.systemDefault()))
            .build();
    }

    private ParsedSetPingMessage parseWithLLM(
        UpdateContext updateContext,
        ChatConfiguration chatConfiguration
    ) {
        // String promptV1 = "Ты — опытный проектный менеджер. Тебя попросили в определённое время напомнить («пингануть») участникам команды в Телеграмм о выполнении конкретной задачи. Твоя задача привести исходную просьбу к тексту формата: 'task_id' 'YYYY-MM-DDTHH:MM:SS(time_ping)' '@username1, @username2, ...'. Отвечай только в таком формате, если информации не достаточно, ответь: \"Мало данных\". ИСХОДНЫЙ ТЕКСТ: ";
        String prompt = "Ты — опытный проектный менеджер. Тебя попросили в определённое время напомнить («пингануть») участникам команды в Телеграмм о выполнении конкретной задачи. Твоя задача привести исходную просьбу к тексту формата: 'task_id' 'YYYY-MM-DDTHH:MM:SS(time_ping)' '@username1, @username2, ...'. Время и дату могут задавать словесно, к примеру 'До 6 вечера', что означает сегодняшний деньT18:00:00 или же фраза 'до 28 числа' может означать что нужно сделать до 28 числа этого же месяца (Пример сегодня 2025-09-12, значит получиться дата для задачи следующая: 2025-09-28). Сегодняшняя дата: " + ZonedDateTime.now().toString() + " Отвечай только в таком формате, если информации не достаточно, ответь: \"Мало данных\". Не используй в ответе ковычки. ИСХОДНЫЙ ТЕКСТ: ";
        String requestLLM = "Контекст: " + chatConfiguration.getConfiguration() + "\n" 
            + prompt + updateContext.getTextMessage();
        String responseLLM = llmAdapter.ask(requestLLM);
        log.debug(responseLLM);

        return parseSetPingMessage(
            responseLLM, 
            updateContext.getChatId(), 
            updateContext.getMessageId()
        );
    }

    private void setPing(UpdateContext updateContext) {

        ChatConfiguration chatConfig = configurateBotService.getChatConfig(updateContext.getChatId());
        ParsedSetPingMessage params;
        try {
            params = parseWithLLM(updateContext, chatConfig);
            // params = parseSetPingMessage(
            //     updateContext.getTextMessage(),
            //     updateContext.getChatId(),
            //     updateContext.getMessageId()
            // );
        } catch(Exception e) {
            log.warn("{}", e.getMessage());
            chatBotAdapter.sendMessage(BotMessage.builder()
                    .chatId(updateContext.getChatId())
                    .replyToMessageId(updateContext.getMessageId())
                    .textMessage(String.format("Произошла ошибка при парсинге команды"))
                .build());
            return;
        }

        JobDataMap data = new JobDataMap();
        data.put("setPingDto", params.getSetPingDto());

        String jobName = generatePingJobId();
        JobDetail job = JobBuilder.newJob(PingJob.class)
                .withIdentity(jobName, "pingByTaskGroup")
                .usingJobData(data)
            .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("pingTrigger_" + jobName, "pingByTaskGroup")
                .startAt(Date.from(params.getZonedDateTime().toInstant()))
            .build();

        try {
            scheduler.scheduleJob(job, trigger);
            jobBindingService.save(JobBindingChat.builder()
                    .chatId(updateContext.getChatId())
                    .jobId(jobName)
                .build());   

            chatBotAdapter.sendMessage(BotMessage.builder()
                    .chatId(updateContext.getChatId())
                    .replyToMessageId(updateContext.getMessageId())
                    .textMessage(String.format("Пинг создан с номером #%s", jobName))
                .build());
        } catch(SchedulerException e) {
            log.error("Error create ping: {}", e.getMessage());
            chatBotAdapter.sendMessage(BotMessage.builder()
                    .chatId(updateContext.getChatId())
                    .replyToMessageId(updateContext.getMessageId())
                    .textMessage("Не удалось создать пинг")
                .build());
        }
    }

    private void deletePing(UpdateContext updateContext) {
        Optional<JobBindingChat> opJobBindChat = jobBindingService.getByJobId(updateContext.getTextMessage());

        if(opJobBindChat.isEmpty() 
            || !opJobBindChat.get().getChatId().equals(updateContext.getChatId())) 
        {
            chatBotAdapter.sendMessage(BotMessage.builder()
                    .chatId(updateContext.getChatId())
                    .replyToMessageId(updateContext.getMessageId())
                    .textMessage("Не удалось найти пинг #" + updateContext.getTextMessage())
                .build());
            return;
        }
        String jobId = opJobBindChat.get().getJobId();

        try {
            jobBindingService.cleanupJobBinding(jobId);
            scheduler.deleteJob(new JobKey(jobId, "pingByTaskGroup"));
            chatBotAdapter.sendMessage(BotMessage.builder()
                    .chatId(updateContext.getChatId())
                    .replyToMessageId(updateContext.getMessageId())
                    .textMessage(String.format("Пинг с номером #%s, успешно удалён", jobId))
                .build());
        } catch(SchedulerException e) {
            log.error("Error create ping: {}", e.getMessage());
            chatBotAdapter.sendMessage(BotMessage.builder()
                    .chatId(updateContext.getChatId())
                    .replyToMessageId(updateContext.getMessageId())
                    .textMessage("Не удалось удалить пинг")
                .build());
        }
    }

    private String generatePingJobId() {
        return String.valueOf(System.currentTimeMillis());
    }
}
