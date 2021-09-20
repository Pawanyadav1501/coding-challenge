package com.magnificent.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@ResponseBody
public class MagnificentController {

    private static final Logger logger = LoggerFactory.getLogger(MagnificentController.class);

    @GetMapping("/")
    public ResponseEntity<String> createOrder()
    {
        if(this.isSuccess()){
            return new ResponseEntity<String>("Success", HttpStatus.OK);
        } else {
            return new ResponseEntity<String>("Failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isSuccess(){
        Boolean[] arr={Boolean.TRUE,Boolean.TRUE,Boolean.TRUE,Boolean.TRUE,Boolean.TRUE,Boolean.FALSE};
        Random r=new Random();
        int randomNumber=r.nextInt(arr.length);
        return arr[randomNumber];
    }

}
