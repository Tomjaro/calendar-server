package com.fiveelements.calendar.record.controller;
import com.fiveelements.calendar.common.ApiResponse;import com.fiveelements.calendar.record.domain.RecordModels.*;import com.fiveelements.calendar.record.service.UserRecordService;import com.fiveelements.calendar.security.SecurityContext;import jakarta.validation.Valid;import java.util.List;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/app-api/records") public class UserRecordController {
 private final UserRecordService service;public UserRecordController(UserRecordService service){this.service=service;}private long uid(){return SecurityContext.currentUser().userId();}
 @GetMapping public ApiResponse<List<RecordView>> records(@RequestParam(defaultValue="") String keyword,@RequestParam(defaultValue="") String tag){return ApiResponse.ok(service.records(uid(),keyword,tag));}
 @GetMapping("/tags") public ApiResponse<List<TagView>> tags(){return ApiResponse.ok(service.tags(uid()));}
 @GetMapping("/{id}") public ApiResponse<RecordView> record(@PathVariable long id){return ApiResponse.ok(service.record(uid(),id));}
 @PostMapping public ApiResponse<RecordView> create(@Valid @RequestBody CreateRecordRequest request){return ApiResponse.ok(service.create(uid(),request));}
 @PutMapping("/{id}") public ApiResponse<RecordView> update(@PathVariable long id,@Valid @RequestBody CreateRecordRequest request){return ApiResponse.ok(service.update(uid(),id,request));}
 @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable long id){service.delete(uid(),id);return ApiResponse.ok(null);}
}
