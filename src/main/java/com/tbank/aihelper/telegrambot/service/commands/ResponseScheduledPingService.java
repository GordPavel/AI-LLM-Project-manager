package com.tbank.aihelper.telegrambot.service.commands;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.tbank.aihelper.llm.LLMAdapter;
import com.tbank.aihelper.telegrambot.dto.UpdateContext;
import com.tbank.aihelper.telegrambot.entity.ChatConfiguration;
import com.tbank.aihelper.telegrambot.entity.JobBindingChat;
import com.tbank.aihelper.telegrambot.entity.ResponseStatusTask;
import com.tbank.aihelper.telegrambot.entity.TaskStatus;
import com.tbank.aihelper.telegrambot.entity.TgUser;
import com.tbank.aihelper.telegrambot.exception.LLMDataDeficitException;
import com.tbank.aihelper.telegrambot.exception.LLMOutputParseException;
import com.tbank.aihelper.telegrambot.observer.EventListenerChatBot;
import com.tbank.aihelper.telegrambot.observer.ObserverChatBotAdapter;
import com.tbank.aihelper.telegrambot.repository.ResponseStatusTaskRepository;
import com.tbank.aihelper.telegrambot.repository.TaskStatusRepository;
import com.tbank.aihelper.telegrambot.service.CommandUtilsService;
import com.tbank.aihelper.telegrambot.service.JobBindingService;
import com.tbank.aihelper.telegrambot.service.UserService;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class ResponseScheduledPingService implements EventListenerChatBot {
    
    private static final String HANDLE_COMMAND = "/response_ping";
    private static final String LLM_DEFICIT_MSG = "Мало данных";
    
    private final ObserverChatBotAdapter observerChatBotAdapter;
    private final ConfigurateBotService configurateBotService;
    private final JobBindingService jobBindingService;
    private final UserService userService;
    private final ResponseStatusTaskRepository responseStatusTaskRepository;
    private final TaskStatusRepository taskStatusRepository;
    private final CommandUtilsService commandUtils;
    private final LLMAdapter llmAdapter;

    @PostConstruct
    private void subscribeToObserver() {
        observerChatBotAdapter.subscribe(this);
    }

    public void handleNewMessage(UpdateContext updateContext) {
        if(!commandUtils.shouldHandle(updateContext, HANDLE_COMMAND))
            return;
        commandUtils.ensureNonEmptyMessage(updateContext, HANDLE_COMMAND);   

        ChatConfiguration chatConfig = configurateBotService.getChatConfig(updateContext.getChatId());

        ResponseStatusTask rst = parseWithLLM(updateContext, chatConfig);
        responseStatusTaskRepository.save(rst);

        commandUtils.fastSend(
            updateContext, 
            "Статус выполнения задачи был помечен как: '" + rst.getIdentifiedLLMStatus().getStatus() + "'\n"
                + rst.getAdviceLLM() 
        );
    }

    private ResponseStatusTask parseWithLLM(
        UpdateContext updateContext,
        ChatConfiguration chatConfiguration
    ) {
        String enumValues = String.join(", ", 
            Arrays.stream(TaskStatus.Status.values())
                .map(Enum::name)
            .toArray(String[]::new)
        );
        
        // String promptV1 = "Ты — опытный проектный менеджер. Проанализируй сообщение пользователя и определить, на каком этапе жизненного цикла выполнения задачи или пинга он находится. Возможные этапы: \n" 
        //     + enumValues + ".\n\n"
        //     + "Если пользователь явно указал либо идентификатор пинга (например, «пинг #2», «ping_id=2», «/response_ping касательно пинга #2»), либо идентификатор задачи (например, «задача #5», «task_id=5»), — тогда выведи ответ в формате из четырёх строк:\n"
        //     + "\n"
        //         + "число ping_id, если оно указано; иначе null.\n"
        //         + "строка/слово task_id, если она указано; иначе null\n"
        //         + "один из статусов: " + enumValues + "\n"
        //         + "краткий (1–2 предложения), конкретный совет, основанный на лучших практиках управления задачами. Обязательно упомяни в совете тот идентификатор, который присутствует (ping_id или task_id). Если оба поля — null, не формируй совет, а верни «Мало данных».\n"
        //     + "\n"
        //     + "ВАЖНО: Не путай пинг и задачу это разные идентификаторы. Так же если ОБА идентификатора null (если один из идентификаторов null тогда данных достаточно) или из сообщения невозможно однозначно определить этап выполнения задачи, выведи только:"
        //     + "\n" + LLM_DEFICIT_MSG;
        String prompt = "Ты — опытный проектный менеджер. Проанализируй сообщение пользователя и определи, на каком этапе жизненного цикла выполнения задачи он находится. Возможные этапы:\n" 
            + enumValues + ".\n\n"
            + "Если пользователь явно указал идентификатор пинга (например, «пинг #2», «ping_id=2», «/response_ping касательно пинга #2»), — тогда выведи ответ в формате из четырёх строк:\n"
            + "\n"
                + "число ping_id, если оно указано; иначе null.\n"
                + "null\n"
                + "один из статусов: " + enumValues + "\n"
                + "краткий (1–2 предложения), конкретный совет, основанный на лучших практиках управления задачами. Обязательно упомяни в совете ping_id, если он присутствует. Если ping_id — null, не формируй совет, а верни «Мало данных».\n"
            + "\n"
            + "Важно: если идентификатор пинга отсутствует (то есть первая строка — null), ответ должен быть только:"
            + "\n" + LLM_DEFICIT_MSG;

        String requestLLM = "Контекст: " + chatConfiguration.getConfiguration() + "\n\n" 
            + prompt + "\n\n" 
            + "Исходный текст:" + updateContext.getTextMessage();

        String responseLLM = llmAdapter.ask(requestLLM);
        log.debug(responseLLM);

        if(responseLLM.split("\n")[0].equals(LLM_DEFICIT_MSG))
            throw new LLMDataDeficitException(
                updateContext.getTextMessage(), 
                HANDLE_COMMAND, 
                updateContext.getChatId()
            );

        try {
            return createResponseStatusTask(
                responseLLM.lines().toList(), 
                updateContext
            );
        } catch (Exception e) {
            throw new LLMOutputParseException(
                responseLLM, 
                HANDLE_COMMAND, 
                updateContext.getChatId()
            );
        }
    }

    private ResponseStatusTask createResponseStatusTask(List<String> params, UpdateContext ctx) {
        JobBindingChat jbc = findBindedJob(params.get(0), params.get(1), ctx.getChatId()); 

        TgUser user = userService.getOrCreateUser(ctx.getUserName());

        TaskStatus taskStatus = taskStatusRepository.findByStatus(TaskStatus.Status.valueOf(params.get(2)))
            .orElseThrow(() -> new LLMOutputParseException(params.get(2), HANDLE_COMMAND, ctx.getChatId()));

        return ResponseStatusTask.builder()
                .jobBindedChat(jbc)
                .user(user)
                .adviceLLM(params.get(3))
                .identifiedLLMStatus(taskStatus)
            .build();
    }

    private JobBindingChat findBindedJob(String jobId, String taskId, Long chatId) {
        return jobBindingService.getByJobId(jobId, chatId);
    }
}
