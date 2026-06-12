package com.qsl.tracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qsl.tracker.domain.PrintTemplate;
import com.qsl.tracker.dto.PrintTemplateRequest;
import com.qsl.tracker.dto.PrintTemplateResponse;
import java.util.List;

public interface PrintTemplateService extends IService<PrintTemplate> {

    List<PrintTemplateResponse> listByUser(String templateType);

    PrintTemplateResponse detail(Long id);

    PrintTemplateResponse create(PrintTemplateRequest request);

    PrintTemplateResponse update(PrintTemplateRequest request);

    void delete(Long id);

    void setDefault(Long id);
}
