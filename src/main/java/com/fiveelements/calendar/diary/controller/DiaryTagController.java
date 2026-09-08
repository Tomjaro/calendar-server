import com.fiveelements.calendar.common.ApiResponse;
import com.fiveelements.calendar.diary.domain.DiaryQueryModels.TagView;
import com.fiveelements.calendar.diary.service.DiaryQueryService;
import com.fiveelements.calendar.diary.service.DiaryService;
import com.fiveelements.calendar.security.SecurityContext;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app-api/diaries/tags")
public class DiaryTagController {
  private final DiaryQueryService queryService;
  private final DiaryService diaryService;

  public DiaryTagController(DiaryQueryService queryService, DiaryService diaryService) {
    this.queryService = queryService;
    this.diaryService = diaryService;
  }

  @GetMapping
  public ApiResponse<List<TagView>> list() {
    return ApiResponse.ok(queryService.tags(SecurityContext.currentUser().userId()));
  }

  @DeleteMapping
  public ApiResponse<Void> delete(@RequestParam String name) {
    diaryService.deleteTag(SecurityContext.currentUser().userId(), name);
    return ApiResponse.ok(null);
  }
}
