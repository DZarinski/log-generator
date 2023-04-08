package com.ad1.loggenerator.service;

import com.ad1.loggenerator.service.implementation.LogService;
import com.ad1.loggenerator.service.implementation.StreamingService;

import static org.hamcrest.CoreMatchers.*;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.context.SpringBootTest;

import com.ad1.loggenerator.exception.FilePathNotFoundException;
import com.ad1.loggenerator.model.BatchSettings;
import com.ad1.loggenerator.model.BatchTracker;
import com.ad1.loggenerator.model.CustomLog;
import com.ad1.loggenerator.model.FieldSettings;
import com.ad1.loggenerator.model.JobStatus;
import com.ad1.loggenerator.model.MalwareSettings;
import com.ad1.loggenerator.model.SelectionModel;
import com.ad1.loggenerator.model.StreamSettings;
import com.ad1.loggenerator.model.StreamTracker;
import com.ad1.loggenerator.model.fieldsettingsmodels.BusinessGuid;
import com.ad1.loggenerator.model.fieldsettingsmodels.CurrentUserId;
import com.ad1.loggenerator.model.fieldsettingsmodels.Disposition;
import com.ad1.loggenerator.model.fieldsettingsmodels.FileSha256;
import com.ad1.loggenerator.model.fieldsettingsmodels.PathToFile;
import com.ad1.loggenerator.model.fieldsettingsmodels.ProcessingTime;
import com.ad1.loggenerator.model.fieldsettingsmodels.TimeStamp;
import com.ad1.loggenerator.service.implementation.AWSStreamService;
import com.ad1.loggenerator.service.implementation.BatchService;
import com.ad1.loggenerator.service.implementation.LogService;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.AdditionalMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StreamingServiceTest {
    
    private StreamingService streamingService;
    private LogService logService;
    private AWSStreamService awsStreamService;

    private String jobId;
    private double repeatingLoglinesPercentage;
    private FieldSettings fieldSettings;
    private MalwareSettings malwareSettings;
    private String mode;
    private StreamSettings streamSettings;
    private BatchSettings batchSettings;
    private List<CustomLog> customLogs;

    private SelectionModel selectionModel;
    
    private StreamTracker streamTracker0;
    private StreamTracker streamTracker1;
    private StreamTracker streamTracker2;

    private final String streamModeFolder = "C:\\log-generator\\stream\\";
    private final String jsonRegexString = "^\\[((\\{(\".*\":.*)*\\})(,\\s)?)*\\]$";
    private final String uuidRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    @BeforeEach
    public void setUp() {
        
        logService = new LogService();
        awsStreamService = mock(AWSStreamService.class);
        streamingService = new StreamingService(logService, awsStreamService);

        // Set up for all SelectioNModels
        jobId = "1";
        repeatingLoglinesPercentage = 0;
        malwareSettings = new MalwareSettings(true, true, true);
        mode = "Stream";
        streamSettings = new StreamSettings("", 0, true);
        batchSettings = new BatchSettings(0);
        customLogs = new ArrayList<CustomLog>();

        TimeStamp timeStamp = new TimeStamp(true, new ArrayList<Long>());
        ProcessingTime processingTime = new ProcessingTime(true, new ArrayList<Long>());
        CurrentUserId currentUserId = new CurrentUserId(true, new ArrayList<String>());
        BusinessGuid businessGuid = new BusinessGuid(true, new ArrayList<String>());
        PathToFile pathToFile = new PathToFile(true, new ArrayList<String>());
        FileSha256 fileSha256 = new FileSha256(true, new ArrayList<String>());
        Disposition disposition = new Disposition(false, new ArrayList<Integer>());
        fieldSettings = new FieldSettings(
            timeStamp,
            processingTime,
            currentUserId,
            businessGuid,
            pathToFile,
            fileSha256,
            disposition
        );

        selectionModel = new SelectionModel(
            jobId,
            repeatingLoglinesPercentage,
            fieldSettings,
            malwareSettings,
            mode,
            streamSettings,
            batchSettings,
            customLogs
        );

        // Set up for stream tracker of size 0, 1, 2
        streamTracker0 = createStreamTracker(0);
        streamTracker1 = createStreamTracker(1);
        streamTracker2 = createStreamTracker(2);
    }

    @Test
    public void test_streamToFile_streamTrackerShouldHaveCorrectLogCount_1() {
        streamingService.streamToFile(selectionModel, streamTracker0);

        assertEquals(0, streamTracker0.getLogCount(), "Log count in stream tracker should be correct");
    }

    @Test
    public void test_streamToFile_streamTrackerShouldHaveCorrectLogCount_2() {
        streamingService.streamToFile(selectionModel, streamTracker1);

        assertEquals(1, streamTracker1.getLogCount(), "Log count in stream tracker should be correct");
    }

    @Test
    public void test_streamToFile_streamTrackerShouldHaveCorrectLogCount_3() {
        streamingService.streamToFile(selectionModel, streamTracker2);

        assertEquals(2, streamTracker2.getLogCount(), "Log count in stream tracker should be correct");
    }

    @Test
    public void test_streamToFile_streamTrackerShouldHaveCorrectLogCount_4() {
        selectionModel.setRepeatingLoglinesPercent(1);
        streamingService.streamToFile(selectionModel, streamTracker0);

        assertEquals(0, streamTracker0.getLogCount(), "Log count in stream tracker should be correct");
    }

    @Test
    public void test_streamToFile_streamTrackerShouldHaveCorrectLogCount_5() {
        selectionModel.setRepeatingLoglinesPercent(1);
        streamingService.streamToFile(selectionModel, streamTracker1);

        assertEquals(1, streamTracker1.getLogCount(), "Log count in stream tracker should be correct");
    }

    @Test
    public void test_streamToFile_streamTrackerShouldHaveCorrectLogCount_6() {
        selectionModel.setRepeatingLoglinesPercent(1);
        streamingService.streamToFile(selectionModel, streamTracker2);

        assertEquals(2, streamTracker2.getLogCount(), "Log count in stream tracker should be correct");
    }

    @Test
    public void test_streamToFile_streamTrackerShouldHaveFAILEDStatus_7() {
        StreamTracker streamTracker = new StreamTracker();
        streamTracker.setJobId(">");

        assertThrows(
            FilePathNotFoundException.class,
            () -> streamingService.streamMode(selectionModel, streamTracker)
        );
        assertEquals(
            JobStatus.FAILED,
            streamTracker.getStatus(),
            "Status should be FAILED if failure to write to file"
        );
    }

    @Test
    public void test_streamToFile_fileShouldHaveCorrectOutputNoRepeating_8() {
        streamingService.streamToFile(selectionModel, streamTracker0);

        String fileName = streamModeFolder + streamTracker0.getJobId() + ".json";
        File jsonLogFile = new File(fileName);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(jsonLogFile));
            String expected = "[]";
            String actual = reader.readLine();
            assertEquals(expected, actual, "Logs written to file should be correct");
        } catch (IOException e) {
            fail("IOException ocurred while trying to read the file generated");
        }
    }

    @Test
    public void test_streamToFile_fileShouldHaveCorrectOutputNoRepeating_9() {
        streamingService.streamToFile(selectionModel, streamTracker1);
        String fileName = streamModeFolder + streamTracker1.getJobId() + ".json";
        File jsonLogFile = new File(fileName);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(jsonLogFile));
            String line = reader.readLine();
            String actual = "";
            while (line != null) {
                actual = actual + line;
                line = reader.readLine();
            }
            assertTrue(actual.matches(jsonRegexString), "Logs written to file should be correct");
        } catch (IOException e) {
            fail("IOException ocurred while trying to read the file generated");
        }
    }

    @Test
    public void test_streamToFile_fileShouldHaveCorrectOutputNoRepeating_10() {
        streamingService.streamToFile(selectionModel, streamTracker2);
        String fileName = streamModeFolder + streamTracker2.getJobId() + ".json";
        File jsonLogFile = new File(fileName);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(jsonLogFile));
            String line = reader.readLine();
            String actual = "";
            while (line != null) {
                actual = actual + line;
                line = reader.readLine();
            }
            assertTrue(actual.matches(jsonRegexString), "Logs written to file should be correct");
        } catch (IOException e) {
            fail("IOException ocurred while trying to read the file generated");
        }
    }

    @Test
    public void test_streamToFile_fileShouldHaveCorrectOutputRepeating_11() {
        selectionModel.setRepeatingLoglinesPercent(1);
        streamingService.streamToFile(selectionModel, streamTracker0);
        String fileName = streamModeFolder + streamTracker0.getJobId() + ".json";
        File jsonLogFile = new File(fileName);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(jsonLogFile));
            String line = reader.readLine();
            String actual = "";
            while (line != null) {
                actual = actual + line;
                line = reader.readLine();
            }
            assertTrue(actual.matches(jsonRegexString), "Logs written to file should be correct");
        } catch (IOException e) {
            fail("IOException ocurred while trying to read the file generated");
        }
    }

    @Test
    public void test_streamToFile_fileShouldHaveCorrectOutputRepeating_12() {
        selectionModel.setRepeatingLoglinesPercent(1);
        streamingService.streamToFile(selectionModel, streamTracker1);
        String fileName = streamModeFolder + streamTracker1.getJobId() + ".json";
        File jsonLogFile = new File(fileName);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(jsonLogFile));
            String line = reader.readLine();
            String actual = "";
            while (line != null) {
                actual = actual + line;
                line = reader.readLine();
            }
            assertTrue(actual.matches(jsonRegexString), "Logs written to file should be correct");
        } catch (IOException e) {
            fail("IOException ocurred while trying to read the file generated");
        }
    }

    @Test
    public void test_streamToFile_fileShouldHaveCorrectOutputRepeating_13() {
        selectionModel.setRepeatingLoglinesPercent(1);
        streamingService.streamToFile(selectionModel, streamTracker2);
        String fileName = streamModeFolder + streamTracker2.getJobId() + ".json";
        File jsonLogFile = new File(fileName);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(jsonLogFile));
            String line = reader.readLine();
            String actual = "";
            while (line != null) {
                actual = actual + line;
                line = reader.readLine();
            }
            assertTrue(actual.matches(jsonRegexString), "Logs written to file should be correct");
        } catch (IOException e) {
            fail("IOException ocurred while trying to read the file generated");
        }
    }

    @Test
    public void test_streamToFile_correctSelectionModelCallsNoRepeating_14() {
        selectionModel = createSelectionModel(0);

        streamingService.streamToFile(selectionModel, streamTracker0);

        verify(selectionModel, times(2)).getCustomLogs();
        verify(selectionModel, times(1)).getFieldSettings();
    }

    @Test
    public void test_streamToFile_correctSelectionModelCallsNoRepeating_15() {
        selectionModel = createSelectionModel(0);

        streamingService.streamToFile(selectionModel, streamTracker1);

        verify(selectionModel, times(3)).getCustomLogs();
        verify(selectionModel, times(2)).getFieldSettings();
        verify(selectionModel, times(1)).getRepeatingLoglinesPercent();
    }

    @Test
    public void test_streamToFile_correctSelectionModelCallsNoRepeating_16() {
        selectionModel = createSelectionModel(0);

        streamingService.streamToFile(selectionModel, streamTracker2);

        verify(selectionModel, times(4)).getCustomLogs();
        verify(selectionModel, times(3)).getFieldSettings();
        verify(selectionModel, times(2)).getRepeatingLoglinesPercent();
    }

    @Test
    public void test_streamToFile_correctSelectionModelCallsRepeating_17() {
        selectionModel = createSelectionModel(1);

        streamingService.streamToFile(selectionModel, streamTracker0);

        verify(selectionModel, times(2)).getCustomLogs();
        verify(selectionModel, times(1)).getFieldSettings();
    }

    @Test
    public void test_streamToFile_correctSelectionModelCallsRepeating_18() {
        selectionModel = createSelectionModel(1);

        streamingService.streamToFile(selectionModel, streamTracker1);

        verify(selectionModel, times(3)).getCustomLogs();
        verify(selectionModel, times(2)).getFieldSettings();
        verify(selectionModel, times(1)).getRepeatingLoglinesPercent();
    }

    @Test
    public void test_streamToFile_correctSelectionModelCallsRepeating_19() {
        selectionModel = createSelectionModel(1);
        streamingService.streamToFile(selectionModel, streamTracker2);

        verify(selectionModel, times(3)).getCustomLogs();
        verify(selectionModel, times(2)).getFieldSettings();
        verify(selectionModel, times(1)).getRepeatingLoglinesPercent();
    }

    @Test
    public void test_streamToFile_correctLogServiceCallsNoRepeating_20() {
        logService = mock(LogService.class);

        when(logService.generateLogLine(
            any(SelectionModel.class),
            any(Set.class))
        ).thenReturn(new JSONObject());

        streamingService = new StreamingService(logService, null);
        streamingService.streamToFile(selectionModel, streamTracker0);

        verify(logService, times(1))
            .preProcessCustomLogs(any(List.class), any(SelectionModel.class));
        verify(logService, times(1))
            .getMasterFieldsList(any(List.class));
        verify(logService, times(0))
            .generateLogLine(any(SelectionModel.class), any(Set.class));
    }

    @Test
    public void test_streamToFile_correctLogServiceCallsNoRepeating_21() {
        logService = mock(LogService.class);

        when(logService.generateLogLine(
            any(SelectionModel.class),
            any(Set.class))
        ).thenReturn(new JSONObject());

        streamingService = new StreamingService(logService, null);
        streamingService.streamToFile(selectionModel, streamTracker1);

        verify(logService, times(1))
            .preProcessCustomLogs(any(List.class), any(SelectionModel.class));
        verify(logService, times(1))
            .getMasterFieldsList(any(List.class));
        verify(logService, times(1))
            .generateLogLine(any(SelectionModel.class), any(Set.class));
    }

    @Test
    public void test_streamToFile_correctLogServiceCallsNoRepeating_22() {
        logService = mock(LogService.class);

        when(logService.generateLogLine(
            any(SelectionModel.class),
            any(Set.class))
        ).thenReturn(new JSONObject());

        streamingService = new StreamingService(logService, null);
        streamingService.streamToFile(selectionModel, streamTracker2);

        verify(logService, times(1))
            .preProcessCustomLogs(any(List.class), any(SelectionModel.class));
        verify(logService, times(1))
            .getMasterFieldsList(any(List.class));
        verify(logService, times(2))
            .generateLogLine(any(SelectionModel.class), any(Set.class));
    }

    @Test
    public void test_streamToFile_correctLogServiceCallsRepeating_23() {
        logService = mock(LogService.class);

        when(logService.generateLogLine(
            any(SelectionModel.class),
            any(Set.class))
        ).thenReturn(new JSONObject());

        streamingService = new StreamingService(logService, null);

        selectionModel.setRepeatingLoglinesPercent(1);
        streamingService.streamToFile(selectionModel, streamTracker0);

        verify(logService, times(1))
            .preProcessCustomLogs(any(List.class), any(SelectionModel.class));
        verify(logService, times(1))
            .getMasterFieldsList(any(List.class));
        verify(logService, times(0))
            .generateLogLine(any(SelectionModel.class), any(Set.class));
    }

    @Test
    public void test_streamToFile_correctLogServiceCallsRepeating_24() {
        logService = mock(LogService.class);

        when(logService.generateLogLine(
            any(SelectionModel.class),
            any(Set.class))
        ).thenReturn(new JSONObject());

        streamingService = new StreamingService(logService, null);

        selectionModel.setRepeatingLoglinesPercent(1);
        streamingService.streamToFile(selectionModel, streamTracker1);

        verify(logService, times(1))
            .preProcessCustomLogs(any(List.class), any(SelectionModel.class));
        verify(logService, times(1))
            .getMasterFieldsList(any(List.class));
        verify(logService, times(1))
            .generateLogLine(any(SelectionModel.class), any(Set.class));
    }

    @Test
    public void test_streamToFile_correctLogServiceCallsRepeating_25() {
        logService = mock(LogService.class);

        when(logService.generateLogLine(
            any(SelectionModel.class),
            any(Set.class))
        ).thenReturn(new JSONObject());

        streamingService = new StreamingService(logService, null);

        selectionModel.setRepeatingLoglinesPercent(1);
        streamingService.streamToFile(selectionModel, streamTracker2);

        verify(logService, times(1))
            .preProcessCustomLogs(any(List.class), any(SelectionModel.class));
        verify(logService, times(1))
            .getMasterFieldsList(any(List.class));
        verify(logService, times(1))
            .generateLogLine(any(SelectionModel.class), any(Set.class));
    }

    @Test
    public void test_generateJobId_JobIdShouldHaveCorrectFormat_26() {
        String actual = streamingService.generateJobId();

        assertTrue(actual.matches(uuidRegex), "JobId should have correct format");
    }

    public StreamTracker createStreamTracker(int size) {
        StreamTracker streamTracker = mock(StreamTracker.class);

        when(streamTracker.getJobId()).thenCallRealMethod();
        doCallRealMethod().when(streamTracker).setJobId(anyString());
        when(streamTracker.getLogCount()).thenCallRealMethod();
        doCallRealMethod().when(streamTracker).setLogCount(anyInt());
        when(streamTracker.getLastPing()).thenCallRealMethod();
        doCallRealMethod().when(streamTracker).setLastPing(anyLong());
        // Control when the stream job completes
        when(streamTracker.getStatus()).thenAnswer(new Answer() {
            private int count = 0;
            private int total = size;

            public Object answer(InvocationOnMock invocation) {
                if (count < total) {
                    count++;
                    return JobStatus.ACTIVE;
                }
                return JobStatus.COMPLETED;
            }
        });
        doCallRealMethod().when(streamTracker).setStatus(any(JobStatus.class));
        when(streamTracker.getStartTime()).thenCallRealMethod();
        doCallRealMethod().when(streamTracker).setStartTime(anyLong());
        when(streamTracker.getEndTime()).thenCallRealMethod();
        doCallRealMethod().when(streamTracker).setEndTime(anyLong());
        when(streamTracker.getStreamObjectURL()).thenCallRealMethod();
        doCallRealMethod().when(streamTracker).setStreamObjectURL(any(URL.class));

        streamTracker.setJobId(jobId);
        streamTracker.setLogCount(0);
        streamTracker.setLastPing(System.currentTimeMillis() / 1000);
        streamTracker.setStatus(JobStatus.ACTIVE);
        streamTracker.setStartTime(System.currentTimeMillis() / 1000);
        streamTracker.setEndTime(-1);
        streamTracker.setStreamObjectURL(null);

        return streamTracker;
    }

    private SelectionModel createSelectionModel(double repeatingLogLinesPercentage) {
        SelectionModel selectionModel = mock(SelectionModel.class);

        when(selectionModel.getJobId()).thenReturn(jobId);
        when(selectionModel.getRepeatingLoglinesPercent()).thenReturn(repeatingLogLinesPercentage);
        when(selectionModel.getFieldSettings()).thenReturn(fieldSettings);
        when(selectionModel.getMode()).thenReturn(mode);
        when(selectionModel.getStreamSettings()).thenReturn(streamSettings);
        when(selectionModel.getBatchSettings()).thenReturn(batchSettings);
        when(selectionModel.getCustomLogs()).thenReturn(customLogs);

        return selectionModel;
    }
}
