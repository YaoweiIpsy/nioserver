package com.ipsy.simpleNIOServer.controller;

import com.ipsy.simpleNIOServer.application.annotations.Controller;
import com.ipsy.simpleNIOServer.application.annotations.RequestMapping;
import com.ipsy.simpleNIOServer.models.User;
import io.reactivex.rxjava3.core.Single;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller(uri = "/demo")
public class DemoController {

  public static void main(String args[]) throws Exception {
    Single.just(5).doOnSuccess(System.out::println).blockingGet();
  }

  @RequestMapping(uri = "/test/{name}", method = "GET")
  public Single<User> test(int name) {
    log.info("aaasfasf, {}", name);
    return Single.just(new User("aasfa", "afasfa"));
  }
}
