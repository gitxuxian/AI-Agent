package com.example.aiagent.advisors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;


@Component
@Slf4j
public class SimpleLoggerAdvisors implements CallAroundAdvisor, StreamAroundAdvisor {
    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        log.info(advisedRequest.userText());
        return advisedRequest;
    }

    private void observeAfter(AdvisedResponse advisedResponse) {
        log.info(advisedResponse.response().getResult().getOutput().getText());
    }


    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        AdvisedRequest request = this.before(advisedRequest);
        AdvisedResponse advisedResponse = chain.nextAroundCall(request);
        this.observeAfter(advisedResponse);
        return advisedResponse;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        AdvisedRequest request = this.before(advisedRequest);
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(request);
        return new MessageAggregator()
            .aggregateAdvisedResponse(advisedResponses
                , advisedResponse -> this.observeAfter(advisedResponse));
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
