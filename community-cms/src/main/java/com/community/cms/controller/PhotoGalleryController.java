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

import java.io.IOException;
import java.util.List;

/**
 * Контроллер для управления фото-галереей в административной панели.
 * Обеспечивает CRUD операции для элементов фото-галереи.
 *
 * Controller for managing photo gallery in admin panel.
 * Provides CRUD operations for photo gallery items.
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 */
@Controller
@RequestMapping("/admin/photo-gallery")
public class PhotoGalleryController {

    private static final Logger logger = LoggerFactory.getLogger(PhotoGalleryController.class);

    // Максимальное количество изображений для загрузки за раз
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

    /**
     * Отображает список всех элементов фото-галереи.
     * Displays list of all photo gallery items.
     *
     * @param model модель для передачи данных в шаблон / model for passing data to template
     * @return имя шаблона / template name
     */
    @GetMapping("")
    public String listPhotoGalleryItems(Model model) {
        logger.info("Запрос списка элементов фото-галереи / Requesting photo gallery items list");

        List<PhotoGalleryItem> items = photoGalleryService.getAllPhotoGalleryItems();
        List<PublicationCategory> categories = publicationCategoryService.getAllCategories();

        model.addAttribute("items", items);
        model.addAttribute("categories", categories);
        model.addAttribute("totalItems", items.size());

        // Добавляем статистику
        var statistics = photoGalleryService.getPhotoGalleryStatistics();
        model.addAttribute("statistics", statistics);

        logger.info("Найдено элементов: {} / Found items: {}", items.size(), items.size());
        return "admin/photo-gallery/list";
    }

    /**
     * Отображает опубликованные элементы фото-галереи.
     * Displays published photo gallery items.
     *
     * @param model модель для передачи данных в шаблон / model for passing data to template
     * @return имя шаблона / template name
     */
    @GetMapping("/published")
    public String listPublishedPhotoGalleryItems(Model model) {
        logger.info("Запрос списка опубликованных элементов / Requesting published items list");

        List<PhotoGalleryItem> items = photoGalleryService.getPublishedPhotoGalleryItems();

        model.addAttribute("items", items);
        model.addAttribute("totalItems", items.size());
        model.addAttribute("isPublishedView", true);

        logger.info("Найдено опубликованных элементов: {} / Found published items: {}", items.size(), items.size());
        return "admin/photo-gallery/list";
    }

    /**
     * Фильтрует элементы по категории.
     * Filters items by category.
     *
     * @param categoryId ID категории / category ID
     * @param model модель для передачи данных в шаблон / model for passing data to template
     * @return имя шаблона / template name
     */
    @GetMapping("/category/{categoryId}")
    public String filterByCategory(@PathVariable Long categoryId, Model model) {
        logger.info("Фильтрация элементов по категории ID: {} / Filtering items by category ID: {}", categoryId, categoryId);

        PublicationCategory category = publicationCategoryService.getCategoryById(categoryId);
        if (category == null) {
            logger.warn("Категория не найдена ID: {} / Category not found ID: {}", categoryId, categoryId);
            return "redirect:/admin/photo-gallery";
        }

        List<PhotoGalleryItem> items = photoGalleryService.getPublishedPhotoGalleryItemsByCategory(category.getName());
        List<PublicationCategory> categories = publicationCategoryService.getAllCategories();

        model.addAttribute("items", items);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("totalItems", items.size());

        logger.info("Найдено элементов в категории '{}': {} / Found items in category '{}': {}",
                category.getName(), items.size(), category.getName(), items.size());
        return "admin/photo-gallery/list";
    }

    /**
     * Фильтрует элементы по году.
     * Filters items by year.
     *
     * @param year год для фильтрации / year for filtering
     * @param model модель для передачи данных в шаблон / model for passing data to template
     * @return имя шаблона / template name
     */
    @GetMapping("/year/{year}")
    public String filterByYear(@PathVariable Integer year, Model model) {
        logger.info("Фильтрация элементов по году: {} / Filtering items by year: {}", year, year);

        List<PhotoGalleryItem> items = photoGalleryService.getPublishedPhotoGalleryItemsByYear(year);
        List<PublicationCategory> categories = publicationCategoryService.getAllCategories();

        model.addAttribute("items", items);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedYear", year);
        model.addAttribute("totalItems", items.size());

        logger.info("Найдено элементов за год {}: {} / Found items for year {}: {}", year, items.size(), year, items.size());
        return "admin/photo-gallery/list";
    }

    // ========== СОЗДАНИЕ ЭЛЕМЕНТА ==========

    /**
     * Отображает форму создания нового элемента фото-галереи.
     * Displays form for creating new photo gallery item.
     *
     * @param model модель для передачи данных в шаблон / model for passing data to template
     * @return имя шаблона / template name
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        logger.info("Отображение формы создания элемента / Displaying create form");

        PhotoGalleryItem item = new PhotoGalleryItem();
        List<PublicationCategory> categories = publicationCategoryService.getAllCategories();

        model.addAttribute("photoGalleryItem", item);
        model.addAttribute("categories", categories);
        model.addAttribute("maxFiles", MAX_UPLOAD_FILES);
        model.addAttribute("isEdit", false);

        return "admin/photo-gallery/create-or-edit";
    }

    /**
     * Обрабатывает создание нового элемента фото-галереи.
     * Processes creation of new photo gallery item.
     *
     * @param item создаваемый элемент / item to create
     * @param bindingResult результаты валидации / validation results
     * @param images загружаемые изображения / uploaded images
     * @param categoryIds IDs выбранных категорий / selected category IDs
     * @param redirectAttributes атрибуты для редиректа / redirect attributes
     * @param model модель для передачи данных в шаблон / model for passing data to template
     * @return перенаправление или имя шаблона / redirect or template name
     */
    @PostMapping("/create")
    public String createPhotoGalleryItem(
            @Valid @ModelAttribute("photoGalleryItem") PhotoGalleryItem item,
            BindingResult bindingResult,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            RedirectAttributes redirectAttributes,
            Model model) {

        logger.info("Обработка создания элемента: {} / Processing item creation: {}", item.getTitle(), item.getTitle());

        // Проверяем валидацию
        if (bindingResult.hasErrors()) {
            logger.warn("Ошибки валидации при создании элемента / Validation errors during item creation");
            return prepareCreateOrEditModel(model, item, false);
        }

        // Проверяем что выбрана хотя бы одна категория
        if (categoryIds == null || categoryIds.isEmpty()) {
            bindingResult.rejectValue("categories", "error.photoGalleryItem",
                    "Выберите хотя бы одну категорию публикации / Select at least one publication category");
            logger.warn("Не выбраны категории для элемента / No categories selected for item");
            return prepareCreateOrEditModel(model, item, false);
        }

        // Проверяем что загружено хотя бы одно изображение
        if (images == null || images.length == 0) {
            bindingResult.rejectValue("images", "error.photoGalleryItem",
                    "Загрузите хотя бы одно изображение / Upload at least one image");
            logger.warn("Не загружены изображения для элемента / No images uploaded for item");
            return prepareCreateOrEditModel(model, item, false);
        }

        // Проверяем лимит файлов
        if (images.length > MAX_UPLOAD_FILES) {
            bindingResult.rejectValue("images", "error.photoGalleryItem",
                    String.format("Максимальное количество изображений: %d / Maximum number of images: %d",
                            MAX_UPLOAD_FILES, MAX_UPLOAD_FILES));
            logger.warn("Превышен лимит файлов: {} > {} / File limit exceeded: {} > {}",
                    images.length, MAX_UPLOAD_FILES, images.length, MAX_UPLOAD_FILES);
            return prepareCreateOrEditModel(model, item, false);
        }

        try {
            // Добавляем выбранные категории
            addSelectedCategories(item, categoryIds);

            // Создаем элемент с изображениями
            PhotoGalleryItem savedItem = photoGalleryService.createPhotoGalleryItemWithImages(item, images);

            // Добавляем сообщение об успехе
            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Элемент '%s' успешно создан / Item '%s' created successfully",
                            savedItem.getTitle(), savedItem.getTitle()));

            logger.info("Элемент успешно создан. ID: {} / Item created successfully. ID: {}",
                    savedItem.getId(), savedItem.getId());

            return "redirect:/admin/photo-gallery";

        } catch (IllegalArgumentException e) {
            // Обработка бизнес-ошибок (лимиты, типы файлов и т.д.)
            logger.error("Ошибка при создании элемента: {} / Error creating item: {}", e.getMessage(), e.getMessage());
            bindingResult.rejectValue("images", "error.photoGalleryItem", e.getMessage());
            return prepareCreateOrEditModel(model, item, false);

        } catch (IOException | FileStorageService.FileStorageException e) {
            // Обработка ошибок файловой системы
            logger.error("Ошибка при загрузке файлов: {} / File upload error: {}", e.getMessage(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при загрузке файлов: " + e.getMessage() + " / File upload error: " + e.getMessage());
            return "redirect:/admin/photo-gallery/create";
        }
    }

    // ========== РЕДАКТИРОВАНИЕ ЭЛЕМЕНТА ==========

    /**
     * Отображает форму редактирования элемента фото-галереи.
     * Displays form for editing photo gallery item.
     *
     * @param id ID редактируемого элемента / ID of item to edit
     * @param model модель для передачи данных в шаблон / model for passing data to template
     * @return имя шаблона / template name
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        logger.info("Отображение формы редактирования элемента ID: {} / Displaying edit form for item ID: {}", id, id);

        try {
            PhotoGalleryItem item = photoGalleryService.getPhotoGalleryItemById(id);
            List<PublicationCategory> categories = publicationCategoryService.getAllCategories();

            // Получаем IDs выбранных категорий
            List<Long> selectedCategoryIds = item.getCategories().stream()
                    .map(PublicationCategory::getId)
                    .toList();

            model.addAttribute("photoGalleryItem", item);
            model.addAttribute("categories", categories);
            model.addAttribute("selectedCategoryIds", selectedCategoryIds);
            model.addAttribute("maxFiles", MAX_UPLOAD_FILES);
            model.addAttribute("isEdit", true);
            model.addAttribute("currentImageCount", item.getImagesCount());

            logger.info("Форма редактирования загружена для элемента ID: {} / Edit form loaded for item ID: {}", id, id);
            return "admin/photo-gallery/create-or-edit";

        } catch (EntityNotFoundException e) {
            logger.warn("Элемент не найден ID: {} / Item not found ID: {}", id, id);
            return "redirect:/admin/photo-gallery";
        }
    }

    /**
     * Обрабатывает обновление элемента фото-галереи.
     * Processes update of photo gallery item.
     *
     * @param id ID обновляемого элемента / ID of item to update
     * @param item обновленные данные элемента / updated item data
     * @param bindingResult результаты валидации / validation results
     * @param images новые загружаемые изображения / new uploaded images
     * @param categoryIds IDs выбранных категорий / selected category IDs
     * @param redirectAttributes атрибуты для редиректа / redirect attributes
     * @param model модель для передачи данных в шаблон / model for passing data to template
     * @return перенаправление или имя шаблона / redirect or template name
     */
    @PostMapping("/edit/{id}")
    public String updatePhotoGalleryItem(
            @PathVariable Long id,
            @Valid @ModelAttribute("photoGalleryItem") PhotoGalleryItem item,
            BindingResult bindingResult,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            RedirectAttributes redirectAttributes,
            Model model) {

        logger.info("Обработка обновления элемента ID: {} / Processing update for item ID: {}", id, id);

        // Проверяем валидацию
        if (bindingResult.hasErrors()) {
            logger.warn("Ошибки валидации при обновлении элемента ID: {} / Validation errors during update for item ID: {}", id, id);
            return prepareCreateOrEditModel(model, item, true);
        }

        // Проверяем что выбрана хотя бы одна категория
        if (categoryIds == null || categoryIds.isEmpty()) {
            bindingResult.rejectValue("categories", "error.photoGalleryItem",
                    "Выберите хотя бы одну категорию публикации / Select at least one publication category");
            logger.warn("Не выбраны категории для элемента ID: {} / No categories selected for item ID: {}", id, id);
            return prepareCreateOrEditModel(model, item, true);
        }

        // Проверяем лимит файлов
        if (images != null && images.length > 0) {
            try {
                // Получаем текущий элемент для проверки количества изображений
                PhotoGalleryItem currentItem = photoGalleryService.getPhotoGalleryItemById(id);
                int totalImages = currentItem.getImagesCount() + images.length;

                if (totalImages > MAX_UPLOAD_FILES) {
                    bindingResult.rejectValue("images", "error.photoGalleryItem",
                            String.format("Превышен лимит изображений. Текущее: %d, добавляемые: %d, максимум: %d / " +
                                            "Image limit exceeded. Current: %d, to add: %d, maximum: %d",
                                    currentItem.getImagesCount(), images.length, MAX_UPLOAD_FILES,
                                    currentItem.getImagesCount(), images.length, MAX_UPLOAD_FILES));
                    logger.warn("Превышен лимит изображений для элемента ID: {} / Image limit exceeded for item ID: {}", id, id);
                    return prepareCreateOrEditModel(model, item, true);
                }
            } catch (EntityNotFoundException e) {
                logger.error("Элемент не найден при проверке лимита ID: {} / Item not found during limit check ID: {}", id, id);
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Элемент не найден / Item not found");
                return "redirect:/admin/photo-gallery";
            }
        }

        try {
            // Добавляем выбранные категории
            addSelectedCategories(item, categoryIds);

            // Устанавливаем ID
            item.setId(id);

            // Обновляем элемент с изображениями
            PhotoGalleryItem updatedItem = photoGalleryService.updatePhotoGalleryItemWithImages(id, item, images);

            // Добавляем сообщение об успехе
            redirectAttributes.addFlashAttribute("successMessage",
                    String.format("Элемент '%s' успешно обновлен / Item '%s' updated successfully",
                            updatedItem.getTitle(), updatedItem.getTitle()));

            logger.info("Элемент успешно обновлен. ID: {} / Item updated successfully. ID: {}",
                    updatedItem.getId(), updatedItem.getId());

            return "redirect:/admin/photo-gallery";

        } catch (IllegalArgumentException e) {
            // Обработка бизнес-ошибок
            logger.error("Ошибка при обновлении элемента ID: {}: {} / Error updating item ID: {}: {}",
                    id, e.getMessage(), id, e.getMessage());
            bindingResult.rejectValue("images", "error.photoGalleryItem", e.getMessage());
            return prepareCreateOrEditModel(model, item, true);

        } catch (IOException | FileStorageService.FileStorageException e) {
            // Обработка ошибок файловой системы
            logger.error("Ошибка при загрузке файлов для элемента ID: {}: {} / File upload error for item ID: {}: {}",
                    id, e.getMessage(), id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при загрузке файлов: " + e.getMessage() + " / File upload error: " + e.getMessage());
            return "redirect:/admin/photo-gallery/edit/" + id;
        } catch (EntityNotFoundException e) {
            logger.error("Элемент не найден при обновлении ID: {} / Item not found during update ID: {}", id, id);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Элемент не найден / Item not found");
            return "redirect:/admin/photo-gallery";
        }
    }

    // ========== УПРАВЛЕНИЕ ИЗОБРАЖЕНИЯМИ ==========

    /**
     * Удаляет изображение из элемента фото-галереи.
     * Deletes image from photo gallery item.
     *
     * @param itemId ID элемента / item ID
     * @param imageId ID изображения / image ID
     * @param redirectAttributes атрибуты для редиректа / redirect attributes
     * @return перенаправление на форму редактирования / redirect to edit form
     */
    @PostMapping("/{itemId}/image/{imageId}/delete")
    public String deleteImage(
            @PathVariable Long itemId,
            @PathVariable Long imageId,
            RedirectAttributes redirectAttributes) {

        logger.info("Удаление изображения. Элемент ID: {}, изображение ID: {} / Deleting image. Item ID: {}, image ID: {}",
                itemId, imageId, itemId, imageId);

        try {
            boolean deleted = photoGalleryService.removeImageFromPhotoGalleryItem(itemId, imageId);

            if (deleted) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Изображение успешно удалено / Image deleted successfully");
                logger.info("Изображение удалено успешно / Image deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Изображение не найдено / Image not found");
                logger.warn("Изображение не найдено / Image not found");
            }

        } catch (IOException | FileStorageService.FileStorageException e) {
            logger.error("Ошибка при удалении изображения: {} / Error deleting image: {}", e.getMessage(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении изображения: " + e.getMessage() + " / Error deleting image: " + e.getMessage());
        }

        return "redirect:/admin/photo-gallery/edit/" + itemId;
    }

    /**
     * Устанавливает основное изображение для элемента.
     * Sets primary image for item.
     *
     * @param itemId ID элемента / item ID
     * @param imageId ID изображения / image ID
     * @param redirectAttributes атрибуты для редиректа / redirect attributes
     * @return перенаправление на форму редактирования / redirect to edit form
     */
    @PostMapping("/{itemId}/image/{imageId}/set-primary")
    public String setPrimaryImage(
            @PathVariable Long itemId,
            @PathVariable Long imageId,
            RedirectAttributes redirectAttributes) {

        logger.info("Установка основного изображения. Элемент ID: {}, изображение ID: {} / Setting primary image. Item ID: {}, image ID: {}",
                itemId, imageId, itemId, imageId);

        boolean success = photoGalleryService.setPrimaryImage(itemId, imageId);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Основное изображение установлено / Primary image set");
            logger.info("Основное изображение установлено успешно / Primary image set successfully");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Не удалось установить основное изображение / Failed to set primary image");
            logger.warn("Не удалось установить основное изображение / Failed to set primary image");
        }

        return "redirect:/admin/photo-gallery/edit/" + itemId;
    }

    // ========== УДАЛЕНИЕ ЭЛЕМЕНТА ==========

    /**
     * Удаляет элемент фото-галереи.
     * Deletes photo gallery item.
     *
     * @param id ID удаляемого элемента / ID of item to delete
     * @param redirectAttributes атрибуты для редиректа / redirect attributes
     * @return перенаправление на список элементов / redirect to items list
     */
    @PostMapping("/delete/{id}")
    public String deletePhotoGalleryItem(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        logger.info("Удаление элемента фото-галереи ID: {} / Deleting photo gallery item ID: {}", id, id);

        try {
            photoGalleryService.deletePhotoGalleryItem(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Элемент успешно удален / Item deleted successfully");
            logger.info("Элемент удален успешно. ID: {} / Item deleted successfully. ID: {}", id, id);

        } catch (IOException | FileStorageService.FileStorageException e) {
            logger.error("Ошибка при удалении элемента ID: {}: {} / Error deleting item ID: {}: {}",
                    id, e.getMessage(), id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при удалении элемента: " + e.getMessage() + " / Error deleting item: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.warn("Элемент не найден при удалении ID: {} / Item not found during deletion ID: {}", id, id);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Элемент не найден / Item not found");
        }

        return "redirect:/admin/photo-gallery";
    }

    // ========== ПРЕДПРОСМОТР ==========

    /**
     * Отображает предпросмотр элемента фото-галереи.
     * Displays preview of photo gallery item.
     *
     * @param id ID элемента для предпросмотра / ID of item to preview
     * @param model модель для передачи данных в шаблон / model for passing data to template
     * @return имя шаблона / template name
     */
    @GetMapping("/preview/{id}")
    public String previewPhotoGalleryItem(@PathVariable Long id, Model model) {
        logger.info("Предпросмотр элемента ID: {} / Previewing item ID: {}", id, id);

        try {
            PhotoGalleryItem item = photoGalleryService.getPhotoGalleryItemById(id);

            model.addAttribute("item", item);
            model.addAttribute("isPreview", true);

            logger.info("Предпросмотр загружен для элемента ID: {} / Preview loaded for item ID: {}", id, id);
            return "admin/photo-gallery/preview";

        } catch (EntityNotFoundException e) {
            logger.warn("Элемент не найден для предпросмотра ID: {} / Item not found for preview ID: {}", id, id);
            return "redirect:/admin/photo-gallery";
        }
    }

    // ========== УПРАВЛЕНИЕ ПУБЛИКАЦИЕЙ ==========

    /**
     * Публикует элемент фото-галереи.
     * Publishes photo gallery item.
     *
     * @param id ID элемента / item ID
     * @param redirectAttributes атрибуты для редиректа / redirect attributes
     * @return перенаправление на список элементов / redirect to items list
     */
    @PostMapping("/publish/{id}")
    public String publishPhotoGalleryItem(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        logger.info("Публикация элемента ID: {} / Publishing item ID: {}", id, id);

        boolean success = photoGalleryService.publishPhotoGalleryItem(id);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Элемент опубликован / Item published");
            logger.info("Элемент опубликован успешно. ID: {} / Item published successfully. ID: {}", id, id);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Не удалось опубликовать элемент / Failed to publish item");
            logger.warn("Не удалось опубликовать элемент ID: {} / Failed to publish item ID: {}", id, id);
        }

        return "redirect:/admin/photo-gallery";
    }

    /**
     * Снимает с публикации элемент фото-галереи.
     * Unpublishes photo gallery item.
     *
     * @param id ID элемента / item ID
     * @param redirectAttributes атрибуты для редиректа / redirect attributes
     * @return перенаправление на список элементов / redirect to items list
     */
    @PostMapping("/unpublish/{id}")
    public String unpublishPhotoGalleryItem(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        logger.info("Снятие с публикации элемента ID: {} / Unpublishing item ID: {}", id, id);

        boolean success = photoGalleryService.unpublishPhotoGalleryItem(id);

        if (success) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Элемент снят с публикации / Item unpublished");
            logger.info("Элемент снят с публикации успешно. ID: {} / Item unpublished successfully. ID: {}", id, id);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Не удалось снять с публикации элемент / Failed to unpublish item");
            logger.warn("Не удалось снять с публикации элемент ID: {} / Failed to unpublish item ID: {}", id, id);
        }

        return "redirect:/admin/photo-gallery";
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    /**
     * Подготавливает модель для форм создания/редактирования.
     * Prepares model for create/edit forms.
     *
     * @param model модель / model
     * @param item элемент / item
     * @param isEdit режим редактирования / edit mode
     * @return имя шаблона / template name
     */
    private String prepareCreateOrEditModel(Model model, PhotoGalleryItem item, boolean isEdit) {
        List<PublicationCategory> categories = publicationCategoryService.getAllCategories();

        model.addAttribute("photoGalleryItem", item);
        model.addAttribute("categories", categories);
        model.addAttribute("maxFiles", MAX_UPLOAD_FILES);
        model.addAttribute("isEdit", isEdit);

        if (isEdit && item.getId() != null) {
            try {
                PhotoGalleryItem currentItem = photoGalleryService.getPhotoGalleryItemById(item.getId());
                model.addAttribute("currentImageCount", currentItem.getImagesCount());
            } catch (EntityNotFoundException e) {
                // Игнорируем если элемент не найден
            }
        }

        return "admin/photo-gallery/create-or-edit";
    }

    /**
     * Добавляет выбранные категории к элементу.
     * Adds selected categories to item.
     *
     * @param item элемент / item
     * @param categoryIds IDs категорий / category IDs
     */
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