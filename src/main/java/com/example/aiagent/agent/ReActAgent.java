package com.example.aiagent.agent;


import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode(callSuper = true)
public abstract class ReActAgent extends BaseAgent {

    /**
     * 思考
     *
     * @return
     */
    public abstract boolean think();


    /**
     * 行动
     *
     * @return
     */
    public abstract String act();


    /**
     * 执行
     *
     * @return
     */
    @Override
    public String step() {
        try {
            boolean think = think();
            if (!think) {
                return "思考完成，无需行动";
            }
            return act();
        } catch (Exception e) {
            log.error("Agent error", e);
            return "Agent error: " + e.getMessage();
        }
    }
}
