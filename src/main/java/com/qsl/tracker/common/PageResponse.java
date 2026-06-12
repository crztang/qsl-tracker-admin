package com.qsl.tracker.common;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageResponse<T> {

    private long total;
    private long pageNo;
    private long pageSize;
    private List<T> records;
}
