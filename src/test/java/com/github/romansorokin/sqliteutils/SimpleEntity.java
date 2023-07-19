package com.github.romansorokin.sqliteutils;

import com.github.romansorokin.sqliteutils.annotations.Column;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
public class SimpleEntity {
    @Column
    private String id;
    @Column("remote_id")
    private Long remoteId;
    @Column
    private int primitive;
    @Column
    private BigDecimal amount;
    @Column
    private BigDecimal[] notExistsField;
}
