package com.community.cms.service;

import com.community.cms.model.Page;
import com.community.cms.repository.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Сервисный слой для работы со страницами.
 * Содержит бизнес-логику приложения и служит прослойкой между контроллерами и репозиториями.
 *
 * <p>Основные responsibilities:
 * <ul>
 *   <li>Валидация бизнес-правил</li>
 *   <li>Обработка транзакций</li>
 *   <li>Преобразование данных между слоями</li>
 *   <li>Обработка исключительных ситуаций</li>
 * </ul>
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 * @see Page
 * @see PageRepository
 */
@Service
public class PageService {

    private final PageRepository pageRepository;

    /**
     * Конструктор с внедрением зависимости PageRepository.
     * Использует Spring DI для автоматического связывания.
     *
     * @param pageRepository репозиторий для работы с данными страниц
     */
    @Autowired
    public PageService(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    /**
     * Сохраняет новую страницу или обновляет существующую.
     * Выполняет проверку уникальности slug перед сохранением.
     *
     * @param page страница для сохранения
     * @return сохраненная страница
     * @throws IllegalArgumentException если slug уже существует (для новых страниц)
     */
    public Page savePage(Page page) {
        // Проверка уникальности slug для новых страниц
        if (page.getId() == null && pageRepository.existsBySlug(page.getSlug())) {
            throw new IllegalArgumentException("Страница с slug '" + page.getSlug() + "' уже существует");
        }

        return pageRepository.save(page);
    }

    /**
     * Находит страницу по уникальному идентификатору.
     *
     * @param id идентификатор страницы
     * @return Optional содержащий страницу если найдена
     */
    public Optional<Page> findPageById(Long id) {
        return pageRepository.findById(id);
    }

    /**
     * Находит страницу по slug.
     * Используется для построения ЧПУ URL.
     *
     * @param slug уникальный идентификатор страницы
     * @return Optional содержащий страницу если найдена
     */
    public Optional<Page> findPageBySlug(String slug) {
        return pageRepository.findBySlug(slug);
    }

    /**
     * Возвращает все опубликованные страницы, отсортированные по дате создания (сначала новые).
     *
     * @return список опубликованных страниц
     */
    public List<Page> findAllPublishedPages() {
        return pageRepository.findByPublishedTrueOrderByCreatedAtDesc();
    }

    /**
     * Возвращает все страницы (опубликованные и черновики).
     * В основном используется в административной части.
     *
     * @return список всех страниц
     */
    public List<Page> findAllPages() {
        return pageRepository.findAll();
    }

    /**
     * Удаляет страницу по идентификатору.
     *
     * @param id идентификатор страницы для удаления
     * @throws IllegalArgumentException если страница не найдена
     */
    public void deletePage(Long id) {
        if (!pageRepository.existsById(id)) {
            throw new IllegalArgumentException("Страница с ID " + id + " не найдена");
        }
        pageRepository.deleteById(id);
    }

    /**
     * Публикует страницу (устанавливает статус published = true).
     *
     * @param id идентификатор страницы
     * @return обновленная страница
     * @throws IllegalArgumentException если страница не найдена
     */
    public Page publishPage(Long id) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Страница с ID " + id + " не найдена"));

        page.setPublished(true);
        return pageRepository.save(page);
    }

    /**
     * Снимает страницу с публикации (устанавливает статус published = false).
     *
     * @param id идентификатор страницы
     * @return обновленная страница
     * @throws IllegalArgumentException если страница не найдена
     */
    public Page unpublishPage(Long id) {
        Page page = pageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Страница с ID " + id + " не найдена"));

        page.setPublished(false);
        return pageRepository.save(page);
    }

    /**
     * Выполняет поиск страниц по заголовку (без учета регистра).
     *
     * @param title фрагмент заголовка для поиска
     * @return список найденных страниц
     */
    public List<Page> searchPagesByTitle(String title) {
        return pageRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Проверяет существование страницы с указанным slug.
     *
     * @param slug slug для проверки
     * @return true если страница существует, иначе false
     */
    public boolean pageExistsBySlug(String slug) {
        return pageRepository.existsBySlug(slug);
    }
}
