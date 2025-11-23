package com.community.cms.repository;

import com.community.cms.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью Page в базе данных.
 * Предоставляет методы для выполнения CRUD операций и пользовательских запросов.
 *
 * <p>Наследует {@link JpaRepository} который предоставляет стандартные методы:
 * <ul>
 *   <li>{@code save()} - сохранение сущности</li>
 *   <li>{@code findById()} - поиск по идентификатору</li>
 *   <li>{@code findAll()} - получение всех записей</li>
 *   <li>{@code delete()} - удаление сущности</li>
 * </ul>
 *
 * @author Vasickin
 * @version 1.0
 * @since 2025
 * @see Page
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface PageRepository extends JpaRepository<Page, Long> {

    /**
     * Находит страницу по уникальному slug.
     *
     * @param slug уникальный идентификатор страницы для URL
     * @return Optional содержащий страницу если найдена, иначе empty
     * @throws org.springframework.dao.DataAccessException при ошибках доступа к данным
     */
    Optional<Page> findBySlug(String slug);

    /**
     * Находит все опубликованные страницы, отсортированные по дате создания (сначала новые).
     *
     * @return список опубликованных страниц
     */
    List<Page> findByPublishedTrueOrderByCreatedAtDesc();

    /**
     * Находит все страницы по статусу публикации.
     *
     * @param published true для опубликованных, false для черновиков
     * @return список страниц с указанным статусом публикации
     */
    List<Page> findByPublished(Boolean published);

    /**
     * Проверяет существование страницы с указанным slug.
     *
     * @param slug slug для проверки
     * @return true если страница с таким slug существует, иначе false
     */
    boolean existsBySlug(String slug);

    /**
     * Находит страницы по заголовку (поиск с учетом регистра).
     *
     * @param title заголовок для поиска
     * @return список страниц с указанным заголовком
     */
    List<Page> findByTitleContainingIgnoreCase(String title);

    /**
     * Пользовательский запрос для поиска страниц по содержимому.
     * Использует Native SQL запрос для полнотекстового поиска.
     *
     * @param content фрагмент содержимого для поиска
     * @return список страниц содержащих указанный текст
     */
    @Query("SELECT p FROM Page p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :content, '%'))")
    List<Page> findByContentContaining(@Param("content") String content);


    // В PageRepository.java добавляем методы:

    /**
     * Подсчитывает количество страниц по статусу публикации.
     *
     * @param published true для опубликованных, false для черновиков
     * @return количество страниц с указанным статусом
     */
    long countByPublished(Boolean published);

    /**
     * Находит последние N страниц, отсортированных по дате создания (сначала новые).
     *
     * @return список последних страниц
     */
    List<Page> findTop5ByOrderByCreatedAtDesc();

    /**
     * Находит последние страницы с ограничением по количеству.
     *
     * @param limit максимальное количество страниц
     * @return список последних страниц
     */
    @Query("SELECT p FROM Page p ORDER BY p.createdAt DESC LIMIT :limit")
    List<Page> findRecentPages(@Param("limit") int limit);
}
