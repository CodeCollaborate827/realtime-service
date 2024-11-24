package com.chat.realtime_service.controller;

import com.chat.realtime_service.config.KafkaStreamsConfig;
import com.chat.realtime_service.models.UserSessionActivity;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/session")
@Slf4j
public class UserSessionController {

    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    private KafkaStreams kafkaStreams;

    @Autowired
    public UserSessionController(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void kafkaStreams(){
        this.kafkaStreams = this.streamsBuilderFactoryBean.getKafkaStreams();
    }

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<Object>> queryUserSessionActivity(@PathVariable String userId) {
        ReadOnlyKeyValueStore<String, UserSessionActivity> store =
                kafkaStreams.store(
                        StoreQueryParameters.fromNameAndType(
                                KafkaStreamsConfig.USER_SESSION_ACTIVITY_STORE, // The name of your state store
                                QueryableStoreTypes.keyValueStore() // Specify the type of the store
                        )
                );

        UserSessionActivity userSessionActivity = store.get(userId);
        return Mono.just(ResponseEntity.ok(userSessionActivity));
    }
}
