package com.tbank.aihelper.telegrambot.service.commands;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

import com.tbank.aihelper.llm.LLMAdapter;
import com.tbank.aihelper.telegrambot.component.PingJob;
import com.tbank.aihelper.telegrambot.dto.ParsedSetPingMessage;
import com.tbank.aihelper.telegrambot.dto.SetPingDto;
import com.tbank.aihelper.telegrambot.dto.UpdateContext;
import com.tbank.aihelper.telegrambot.entity.ChatConfiguration;
import com.tbank.aihelper.telegrambot.entity.JobBindingChat;
import com.tbank.aihelper.telegrambot.exception.LLMDataDeficitException;
import com.tbank.aihelper.telegrambot.exception.LLMOutputParseException;
import com.tbank.aihelper.telegrambot.observer.EventListenerChatBot;
import com.tbank.aihelper.telegrambot.observer.ObserverChatBotAdapter;
import com.tbank.aihelper.telegrambot.service.CommandUtilsService;
import com.tbank.aihelper.telegrambot.service.JobBindingService;
import com.tbank.aihelper.telegrambot.service.UserService;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
    /set_ping
    Params:
        task_id: String
        YYYY-MM-DDTHH:MM:SS(time_ping): ZonedDateTime
        cron_expression: CronScheduleBuilder
        [@username1, @username2, ...]: List<String>
*/
@Slf4j
@Service
@AllArgsConstructor
public class SetScheduledPingService implements EventListenerChatBot {
    
    private static final String HANDLE_COMMAND = "/set_ping";
    private static final String LLM_DEFICIT_MSG = "Мало данных";

    private final Scheduler scheduler;
    private final JobBindingService jobBindingService;
    private final ObserverChatBotAdapter observerChatBotAdapter;
    private final ConfigurateBotService configurateBotService;
    private final UserService userService;
    private final LLMAdapter llmAdapter;
    private final CommandUtilsService commandUtils;

    @PostConstruct
    private void subscribeToObserver() {
        observerChatBotAdapter.subscribe(this);
    }

    public void handleNewMessage(UpdateContext updateContext) {
        if(!commandUtils.shouldHandle(updateContext, HANDLE_COMMAND))
            return;
        commandUtils.ensureNonEmptyMessage(updateContext, HANDLE_COMMAND);   

        ChatConfiguration chatConfig = configurateBotService.getChatConfig(updateContext.getChatId());
        ParsedSetPingMessage params = parseWithLLM(updateContext, chatConfig);

        JobDataMap data = new JobDataMap();
        data.put("setPingDto", params.getSetPingDto());

        String jobName = jobBindingService.save(JobBindingChat.builder()
                    .chatConfiguration(chatConfig)
                    .taskId(params.getSetPingDto().getTaskId())
                    .performers(
                        params.getSetPingDto().getUsernamesToPing().stream()
                                .map(userService::getOrCreateUser)
                            .toList()
                    )
                .build()
            ).getId().toString();

        JobDetail job = JobBuilder.newJob(PingJob.class)
                .withIdentity(jobName, "pingByTaskGroup")
                .usingJobData(data)
            .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("pingTrigger_" + jobName, "pingByTaskGroup")
                .withSchedule(params.getSchedule())
                .endAt(Date.from(params.getZonedDateTime().toInstant()))
            .build();

        try {
            scheduler.scheduleJob(job, trigger);

            commandUtils.fastSend(
                updateContext, 
                String.format("Пинг создан с номером #%s", jobName)
            );
        } catch(SchedulerException e) {
            log.error("Error create ping: {}", e.getMessage());
            jobBindingService.deleteJobById(Long.valueOf(jobName));

            commandUtils.fastSend(
                updateContext, 
                "Не удалось создать пинг"
            );
        }
    }

    private ParsedSetPingMessage parseWithLLM(
        UpdateContext updateContext,
        ChatConfiguration chatConfiguration
    ) {
        // String promptV1 = "Ты — опытный проектный менеджер. Тебя попросили в определённое время напомнить («пингануть») участникам команды в Телеграмм о выполнении конкретной задачи. Твоя задача привести исходную просьбу к тексту формата: 'task_id' 'YYYY-MM-DDTHH:MM:SS(time_ping)' '@username1, @username2, ...'. Отвечай только в таком формате, если информации не достаточно, ответь: \"Мало данных\". ИСХОДНЫЙ ТЕКСТ: ";
        // String promptV2 = "Ты — опытный проектный менеджер. Тебя попросили в определённое время напомнить («пингануть») участникам команды в Телеграмм о выполнении конкретной задачи. Твоя задача привести исходную просьбу к тексту формата: 'task_id' 'YYYY-MM-DDTHH:MM:SS(time_ping)' '@username1, @username2, ...'. Время и дату могут задавать словесно, к примеру 'До 6 вечера', что означает сегодняшний деньT18:00:00 или же фраза 'до 28 числа' может означать что нужно сделать до 28 числа этого же месяца (Пример сегодня 2025-09-12, значит получиться дата для задачи следующая: 2025-09-28). Сегодняшняя дата: " + ZonedDateTime.now().toString() + " Отвечай только в таком формате, если информации не достаточно, ответь: \"Мало данных\". Не используй в ответе ковычки. ИСХОДНЫЙ ТЕКСТ: ";
        String prompt = "Всегда пиши только итоговый ответ и ничего больше. Ты — опытный проектный менеджер. Тебя попросили в определённое время напомнить («пингануть») участникам команды в Телеграмм о выполнении конкретной задачи. Твоя задача привести исходную просьбу к тексту формата:" 
            + "\n\n"
                + "строка/слово task_id\n"
                + "YYYY-MM-DDTHH:MM:SS(time_ping)\n"
                + "cron_expression\n"
                + "@username1, @username2, ...\n"
            + "\n"
            + "В ответе не указывай названия переменных, только их значения.\n"
            + "Время и дату могут задавать словесно, к примеру 'До 6 вечера', что означает сегодняшний деньT18:00:00 или же фраза 'до 28 числа' может означать что нужно сделать до 28 числа этого же месяца (Пример сегодня 2025-09-12, значит получится дата для задачи следующая: 2025-09-28). Интервал пинга должен быть указан в формате CronScheduleBuilder.cronSchedule из Java Quartz (например: 0 0/15 * * * ? — каждые 15 минут). Если в исходной просьбе интервал не указан — используй разумное значение по умолчанию: 0 0 12 * * ? (ежедневно в 12:00)."
            + "\n\nСегодняшняя дата: " + ZonedDateTime.now().toString() + "\n\n"
            + "Отвечай только в таком формате, каждый параметр с новой строки. Если информации недостаточно, ответь:"
            + "\n\n" + LLM_DEFICIT_MSG + "\n\n"
            + "Не используй в ответе кавычки.";
        
        String requestLLM = "Контекст: " + chatConfiguration.getConfiguration() + "\n\n" 
            + prompt + "\n\n" 
            + "Исходный текст:" + updateContext.getTextMessage();

        String responseLLM = llmAdapter.ask(requestLLM);
        log.debug(responseLLM);

        if(responseLLM.equals(LLM_DEFICIT_MSG))
            throw new LLMDataDeficitException(
                updateContext.getTextMessage(), 
                HANDLE_COMMAND, 
                updateContext.getChatId()
            );
        
        try {
            return parseSetPingMessage(
                responseLLM, 
                updateContext.getChatId(), 
                updateContext.getMessageId()
            );
        } catch (Exception e) {
            throw new LLMOutputParseException(responseLLM, HANDLE_COMMAND, updateContext.getChatId());
        }
    }

    private ParsedSetPingMessage parseSetPingMessage(
        String message,
        Long chatId,
        Integer messageId
    ) {
        List<String> params = message.lines().map(String::trim).toList();
        return ParsedSetPingMessage.builder()
                .setPingDto(SetPingDto.builder()
                        .chatId(chatId)
                        .messageId(messageId)
                        .taskId(params.get(0))
                        .usernamesToPing(Arrays.asList(params.get(3).split(", ")).stream()
                                .filter(word -> word.startsWith("@"))
                                .map(word -> word.substring(1))
                            .toList())
                    .build())
                .zonedDateTime(ZonedDateTime.of(
                    LocalDateTime.parse(params.get(1)), ZoneId.systemDefault()))
                .schedule(CronScheduleBuilder.cronSchedule(params.get(2)))
            .build();
    }
}
