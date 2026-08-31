package com.fiveelements.calendar.file.controller;

import com.fiveelements.calendar.common.ApiResponse;
import com.fiveelements.calendar.file.service.PrivateFileService;
import com.fiveelements.calendar.security.SecurityContext;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/app-api/files")
public class PrivateFileController {
  private final PrivateFileService service;

  public PrivateFileController(PrivateFileService service) {
    this.service = service;
  }

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<PrivateFileService.FileView> upload(@RequestPart("file") MultipartFile file)
      throws Exception {
    return ApiResponse.ok(service.save(SecurityContext.currentUser().userId(), file));
  }

  @GetMapping("/{id}")
  public ResponseEntity<byte[]> get(@PathVariable long id) {
    var file = service.load(SecurityContext.currentUser().userId(), id);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(file.contentType()))
        .cacheControl(CacheControl.noStore())
        .body(file.content());
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@PathVariable long id) {
    service.delete(SecurityContext.currentUser().userId(), id);
    return ApiResponse.ok(null);
  }
}
