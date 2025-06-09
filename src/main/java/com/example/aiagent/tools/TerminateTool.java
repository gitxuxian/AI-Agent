package com.example.aiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class TerminateTool {

    @Tool(name = "doTerminate", description = """  
        Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.  
        When you have finished all the tasks, call this tool to end the work.  
        This tool should be called when:
        1. All user requests have been completed
        2. The task cannot be completed due to constraints
        3. No further action is needed
        """)
    public String doTerminate() {
        return "TERMINATE_SIGNAL:任务结束";
    }
}
