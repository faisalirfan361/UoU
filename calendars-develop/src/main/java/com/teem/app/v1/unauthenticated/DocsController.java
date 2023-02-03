package com.UoU.app.v1.unauthenticated;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/docs")
public class DocsController {

  @GetMapping
  public String index() {
    // Redirect to the static v1 overview page:
    // noinspection SpringMVCViewInspection
    return "redirect:/docs/v1.html";
  }
}
