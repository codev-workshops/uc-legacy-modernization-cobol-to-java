package com.carddemo.batch.job.wait;

import com.carddemo.common.util.WaitUtil;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

/**
 * Tasklet equivalent of COBSWAIT.cbl — accepts a centisecond wait time
 * and delegates to {@link WaitUtil#waitCentiseconds(long)}.
 */
public class WaitTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception {

        Long waitTime = chunkContext.getStepContext()
                .getStepExecution()
                .getJobParameters()
                .getLong("waitTime");

        if (waitTime == null) {
            throw new IllegalArgumentException(
                    "Job parameter 'waitTime' is required");
        }
        if (waitTime <= 0) {
            throw new IllegalArgumentException(
                    "Job parameter 'waitTime' must be positive, got " + waitTime);
        }

        WaitUtil.waitCentiseconds(waitTime);
        return RepeatStatus.FINISHED;
    }
}
