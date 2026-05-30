package com.carddemo.batch.statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Writes formatted account statements to a text file.
 *
 * <p>Mirrors the COBOL CBSTM03A output logic: each {@link StatementData} is
 * rendered via {@link StatementFormatterService} and written as a block of
 * 80-character lines separated by blank lines between statements.
 */
public class StatementWriter implements ItemWriter<StatementData>, StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(StatementWriter.class);

    private final String outputPath;
    private final StatementFormatterService formatterService;
    private BufferedWriter writer;
    private boolean firstStatement = true;

    public StatementWriter(String outputPath, StatementFormatterService formatterService) {
        this.outputPath = outputPath;
        this.formatterService = formatterService;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        try {
            writer = new BufferedWriter(new FileWriter(outputPath));
            log.info("Opened statement file: {}", outputPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open statement file: " + outputPath, e);
        }
    }

    @Override
    public void write(Chunk<? extends StatementData> items) throws Exception {
        for (StatementData data : items) {
            if (!firstStatement) {
                writer.newLine();
            }
            firstStatement = false;

            List<String> lines = formatterService.formatStatement(data);
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        try {
            if (writer != null) {
                writer.close();
                log.info("Closed statement file: {}", outputPath);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error closing statement file", e);
        }
        return ExitStatus.COMPLETED;
    }
}
