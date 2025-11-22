package com.community.cms.service;

import com.community.cms.model.Page;
import com.community.cms.repository.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for page operations.
 * Contains business logic and serves as a bridge between controllers and repositories.
 * Сервисный слой для работы со страницами.
 * Содержит бизнес-логику приложения и служит прослойкой между контроллерами и репозиториями.
 *
 * <p>Main responsibilities / Основные обязанности:
 * <ul>
 *   <li>Business rules validation / Валидация бизнес-правил</li>
 *   <li>Transaction handling / Обработка транзакций</li>
 *   <li>Data transformation between layers / Преобразование данных между слоями</li>
 *   <li>Exception handling / Обработка исключительных ситуаций</li>
 * </ul>
 *
 * @author Vasickin
 * @version 2.0
 * @since 2025
 * @see Page
 * @see PageRepository
 */
@Service
public class PageService {

    private final PageRepository pageRepository;

    /**
     * Constructor with PageRepository dependency injection.
     * Uses Spring DI for automatic wiring.
     *
     * Конструктор с внедрением зависимости PageRepository.
     * Использует Spring DI для автоматического связывания.
     *
     * @param pageRepository repository for page data operations / репозиторий для работы с данными страниц
     */
    @Autowired
    public PageService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    /**
     * Saves a new page or updates an existing one.
     * Performs slug uniqueness check before saving.
     *
     * Сохраняет новую страницу или обновляет существующую.
     * Выполняет проверку уникальности slug перед сохранением.
     *
     * @param page page to save / страница для сохранения
     * @return saved page / сохраненная страница
     * @throws IllegalArgumentException if slug already exists (for new pages) / если slug уже существует (для новых страниц)
     */
    public Page savePage(Page page) {
        // Check slug uniqueness for new pages / Проверка уникальности slug для новых страниц
        if (page.getId() == null && pageRepository.existsBySlug(page.getSlug())) {
            throw new IllegalArgumentException("Page with slug '" + page.getSlug() + "' already exists / Страница с slug '" + page.getSlug() + "' уже существует");
        }

        return pageRepository.save(page);
    }

    /**
     * Finds a page by its unique identifier.
     *
     * Находит страницу по уникальному идентификатору.
     *
     * @param id page identifier / идентификатор страницы
     * @return Optional containing page if found / Optional содержащий страницу если найдена
     */
    public Optional<Page> findPageById(Long id) {
        return pageRepository.findById(id);
    }

    /**
     * Finds a page by slug.
     * Used for SEO-friendly URL building.
     *
     * Находит страницу по slug.
     * Используется для построения ЧПУ URL.
     *
     * @param slug unique page identifier / уникальный идентификатор страницы
     * @return Optional containing page if found / Optional содержащий страницу если найдена
     */
    public Optional<Page> findPageBySlug(String slug) {
        return pageRepository.findBySlug(slug);
    }

    /**
     * Returns all published pages, sorted by sort order (ascending).
     *
     * Возвращает все опубликованные страницы, отсортированные по порядку (по возрастанию).
     *
     * @return list of published pages / список опубликованных страниц
     */
    public List<Page> findAllPublishedPages() {
        return pageRepository.findByIsPublishedTrueOrderBySortOrderAsc();
    }

    /**
     * Returns all pages (both published and drafts).
     * Mainly used in admin interface.
     *
     * Возвращает все страницы (опубликованные и черновики).
     * В основном используется в административной части.
     *
     * @return list of all pages / список всех страниц
     */
    public List<Page> findAllPages() {
        return pageRepository.findAll();
    }

    /**
     * Deletes a page by identifier.
     *
     * Удаляет страницу по идентификатору.
     *
     * @param id page identifier to delete / идентификатор страницы для удаления
     * @throws IllegalArgumentException if page not found / если страница не найдена
     */
    public void deletePage(Long id) {
        if (!pageRepository.existsById(id)) {
            throw new IllegalArgumentException("Page with ID " + id + " not found / Страница с ID " + id + " не найдена");
        }
        pageRepository.deleteById(id);
    }

    /**
     * Publishes a page (sets published status to true).
     *
     * Публикует страницу (устанавливает статус published = true).
     *
     * @param id page identifier / идентификатор страницы
     * @return updated page / обновленная страница
     * @throws IllegalArgumentException if page not found / если страница не найдена
     */
    public Page publishPage(Long id) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Page with ID " + id + " not found / Страница с ID " + id + " не найдена"));

        page.setIsPublished(true);
        return pageRepository.save(page);
    }

    /**
     * Unpublishes a page (sets published status to false).
     *
     * Снимает страницу с публикации (устанавливает статус published = false).
     *
     * @param id page identifier / идентификатор страницы
     * @return updated page / обновленная страница
     * @throws IllegalArgumentException if page not found / если страница не найдена
     */
    public Page unpublishPage(Long id) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Page with ID " + id + " not found / Страница с ID " + id + " не найдена"));

        page.setIsPublished(false);
        return pageRepository.save(page);
    }

    /**
     * Searches pages by slug containing the given string (case-insensitive).
     *
     * Выполняет поиск страниц по slug, содержащему данную строку (без учета регистра).
     *
     * @param slug slug fragment to search for / фрагмент slug для поиска
     * @return list of found pages / список найденных страниц
     */
    public List<Page> searchPagesBySlug(String slug) {
        return pageRepository.findBySlugContainingIgnoreCase(slug);
    }

    /**
     * Checks if a page with the specified slug exists.
     *
     * Проверяет существование страницы с указанным slug.
     *
     * @param slug slug to check / slug для проверки
     * @return true if page exists, false otherwise / true если страница существует, иначе false
     */
    public boolean pageExistsBySlug(String slug) {
        return pageRepository.existsBySlug(slug);
    }
}