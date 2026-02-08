package nelon.arrive.nelonshift.controller;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.entity.Project;
import nelon.arrive.nelonshift.repository.ProjectRepository;
import nelon.arrive.nelonshift.services.ProjectExcelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectExcelController {

  private final ProjectExcelService excelExportService;
  private final ProjectRepository projectRepository;

  /**
   * Экспорт проекта в Excel
   * GET /api/projects/{id}/export/excel
   */
  @GetMapping("/{id}/export/excel")
  public ResponseEntity<byte[]> exportProjectToExcel(@PathVariable Long id) {
    try {
      // Находим проект со всеми сменами
      Project project = projectRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("Проект не найден с ID: " + id));

      // Генерируем Excel
      byte[] excelData = excelExportService.exportProjectToExcel(project);

      // Формируем имя файла
      String fileName = "Проект_" + project.getName().replaceAll("[^a-zA-Zа-яА-Я0-9]", "_") + ".xlsx";
      String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
          .replace("+", "%20");

      // Настраиваем заголовки
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
      headers.setContentDispositionFormData("attachment", encodedFileName);
      headers.set("Content-Disposition", "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName);

      return new ResponseEntity<>(excelData, headers, HttpStatus.OK);

    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
  }
}