package com.fakecompanydetector.service.analysis;

import com.fakecompanydetector.entity.enums.SignalConfidence;
import com.fakecompanydetector.entity.enums.SignalType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignalResult {
    private String signalName;
    private SignalType signalType;
    private Integer points;
    private String explanation;
    private String evidenceJson;
    private SignalConfidence confidence;
    private String dataSource;
}
