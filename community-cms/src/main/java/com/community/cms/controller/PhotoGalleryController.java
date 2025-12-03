package com.community.cms.controller;

import com.community.cms.model.gallery.PhotoGalleryItem;
import com.community.cms.model.gallery.PublicationCategory;
import com.community.cms.service.FileStorageService;
import com.community.cms.service.category.PublicationCategoryService;
import com.community.cms.service.gallery.PhotoGalleryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/photo-gallery")
public class PhotoGalleryController {

    private static final Logger logger = LoggerFactory.getLogger(PhotoGalleryController.class);
    private static final int MAX_UPLOAD_FILES = 15;

    private final PhotoGalleryService photoGalleryService;
    private final PublicationCategoryService publicationCategoryService;
    private final FileStorageService fileStorageService;

    @Autowired
    public PhotoGalleryController(PhotoGalleryService photoGalleryService,
                                  PublicationCategoryService publicationCategoryService,
                                  FileStorageService fileStorageService) {
        this.photoGalleryService = photoGalleryService;
        this.publicationCategoryService = publicationCategoryService;
        this.fileStorageService = fileStorageService;
    }

    // ========== СПИСОК ЭЛЕМЕНТОВ ==========

    @GetMapping("")
    public String listPhotoGalleryItems(Model model) {
        List<PhotoGalleryItem> items = photoGalleryService.getAllPhotoGalleryItems();
        List<PublicationCategory> categories = publicationCategoryService.getAllCategories();

        model.addAttribute("items", items);
        model.addAttribute("categories", categories);
        model.addAttribute("totalItems", items.size());
        model.addAttribute("statistics", photoGalleryService.getPhotoGalleryStatistics());
        model.addAttribute("isPublishedView", false);
        model.addAttribute("selectedCategory", null);
        model.addAttribute("selectedYear", null);

        return "admin/photo-gallery/list";
    }

    // ========== СОЗДАНИЕ ЭЛЕМЕНТА ==========

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        logger.info("Открытие формы создания элемента");

        PhotoGalleryItem item = new PhotoGalleryItem();
        // Устанавливаем текущий год по умолчанию
        item.setYear(java.time.Year.now().getValue());

        List<PublicationCategory> categories = publicationCategoryService.getAllCategories();
        logger.info("Загружено категорий для формы: {}", categories.size());

        model.addAttribute("photoGalleryItem", item);
        model.addAttribute("categories", categories);
        model.addAttribute("maxFiles", MAX_UPLOAD_FILES);
        model.addAttribute("isEdit", false);

        return "admin/photo-gallery/create-or-edit";
    }

    @PostMapping("/create")
    public String createPhotoGalleryItem(
            @Valid @ModelAttribute("photoGalleryItem") PhotoGalleryItem item,
            BindingResult bindingResult,
            @RequestParam(value = "files", required = false) MultipartFile[] files, // ИЗМЕНЕНО: images -> files
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            RedirectAttributes redirectAttributes,
            Model model) {

        logger.info("Создание элемента: {}, категории: {}, файлов: {}",
                item.getTitle(), categoryIds, files != null ? files.length : 0);

        if (bindingResult.hasErrors()) {
            logger.warn("Ошибки валидации: {}", bindingResult.getAllErrors());
            return prepareCreateOrEditModel(model, item, false, categoryIds);
        }

        if (categoryIds == null || categoryIds.isEmpty()) {
            bindingResult.rejectValue("categories", "error.photoGalleryItem",
                    "Выберите хотя бы одну категорию");
            return prepareCreateOrEditModel(model, item, false, categoryIds);
        }

        // Проверяем файлы
        if (files == null || files.length == 0) {
            bindingResult.rejectValue("images", "error.photoGalleryItem",
                    "Загрузите хотя бы одно изображение");
            return prepareCreateOrEditModel(model, item, false, categoryIds);
        }

        try {
            addSelectedCategories(item, categoryIds);
            PhotoGalleryItem savedItem = photoGalleryService.createPhotoGalleryItemWithImages(item, files);

            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Элемент '%s' успешно создан", savedItem.getTitle()));

            return "redirect:/admin/photo-gallery";

        } catch (Exception e) {
            logger.error("Ошибка при создании: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка: " + e.getMessage());
            return "redirect:/admin/photo-gallery/create";
        }
    }

    // ========== РЕДАКТИРОВАНИЕ ЭЛЕМЕНТА ==========

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            PhotoGalleryItem item = photoGalleryService.getPhotoGalleryItemById(id);
            List<PublicationCategory> categories = publicationCategoryService.getAllCategories();

            List<Long> selectedCategoryIds = item.getCategories().stream()
                    .map(PublicationCategory::getId)
                    .toList();

            model.addAttribute("photoGalleryItem", item);
            model.addAttribute("categories", categories);
            model.addAttribute("selectedCategoryIds", selectedCategoryIds);
            model.addAttribute("maxFiles", MAX_UPLOAD_FILES);
            model.addAttribute("isEdit", true);
            model.addAttribute("currentImageCount", item.getImagesCount());

            return "admin/photo-gallery/create-or-edit";

        } catch (EntityNotFoundException e) {
            return "redirect:/admin/photo-gallery";
        }
    }

    @PostMapping("/edit/{id}")
    public String updatePhotoGalleryItem(
            @PathVariable Long id,
            @Valid @ModelAttribute("photoGalleryItem") PhotoGalleryItem item,
            BindingResult bindingResult,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            @RequestParam(value = "deleteExistingImages", required = false) List<Long> deleteImageIds,
            RedirectAttributes redirectAttributes,
            Model model) {

        logger.info("Редактирование элемента ID: {}, категории: {}, файлов: {}",
                id, categoryIds, files != null ? files.length : 0);

        if (bindingResult.hasErrors()) {
            logger.warn("Ошибки валидации: {}", bindingResult.getAllErrors());
            return prepareCreateOrEditModel(model, item, true, categoryIds);
        }

        if (categoryIds == null || categoryIds.isEmpty()) {
            bindingResult.rejectValue("categories", "error.photoGalleryItem",
                    "Выберите хотя бы одну категорию");
            return prepareCreateOrEditModel(model, item, true, categoryIds);
        }

        try {
            addSelectedCategories(item, categoryIds);

            // Удаляем помеченные изображения
            if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
                for (Long imageId : deleteImageIds) {
                    photoGalleryService.removeImageFromPhotoGalleryItem(id, imageId);
                }
            }

            // Обновляем элемент с новыми файлами
            PhotoGalleryItem updatedItem = photoGalleryService.updatePhotoGalleryItemWithImages(
                    id, item, files);

            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Элемент '%s' успешно обновлен", updatedItem.getTitle()));

            return "redirect:/admin/photo-gallery";

        } catch (Exception e) {
            logger.error("Ошибка при обновлении: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка: " + e.getMessage());
            return "redirect:/admin/photo-gallery/edit/" + id;
        }
    }

    // ========== УДАЛЕНИЕ ЭЛЕМЕНТА ==========

    @PostMapping("/delete/{id}")
    public String deletePhotoGalleryItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            photoGalleryService.deletePhotoGalleryItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "Элемент успешно удален");
        } catch (Exception e) {
            logger.error("Ошибка при удалении элемента {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении: " + e.getMessage());
        }
        return "redirect:/admin/photo-gallery";
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private String prepareCreateOrEditModel(Model model, PhotoGalleryItem item, boolean isEdit, List<Long> categoryIds) {
        List<PublicationCategory> categories = publicationCategoryService.getAllCategories();

        model.addAttribute("photoGalleryItem", item);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryIds", categoryIds);
        model.addAttribute("maxFiles", MAX_UPLOAD_FILES);
        model.addAttribute("isEdit", isEdit);

        return "admin/photo-gallery/create-or-edit";
    }

    private void addSelectedCategories(PhotoGalleryItem item, List<Long> categoryIds) {
        if (categoryIds != null) {
            item.getCategories().clear();
            for (Long categoryId : categoryIds) {
                PublicationCategory category = publicationCategoryService.getCategoryById(categoryId);
                if (category != null) {
                    item.addCategory(category);
                }
            }
        }
    }
}