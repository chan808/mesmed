package com.chan.medmes.inspection.entity;

import com.chan.medmes.inspection.enums.InspectionResult;
import com.chan.medmes.material.entity.InspectionSpec;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inspection_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_record_id", nullable = false)
    private InspectionRecord record;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_spec_id", nullable = false)
    private InspectionSpec spec;

    @Column(name = "measured_value", length = 100)
    private String measuredValue;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private InspectionResult result;

    @Builder
    public InspectionDetail(InspectionRecord record, InspectionSpec spec,
                            String measuredValue, InspectionResult result) {
        this.record = record;
        this.spec = spec;
        this.measuredValue = measuredValue;
        this.result = result;
    }
}
