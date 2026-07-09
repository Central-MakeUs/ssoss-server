package com.ssoss.archfixtures.coding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Transactional
public class BadStyleController {

    @Autowired
    private Object dependency;
}
