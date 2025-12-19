package com.community.cms.repository.project;

import com.community.cms.model.project.Project;
import com.community.cms.model.project.TeamMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с сущностью TeamMember в базе данных.
 *
 * <p>Предоставляет методы для выполнения CRUD операций и пользовательских запросов
 * для членов команды организации "ЛАДА". Расширяет стандартный JpaRepository и добавляет
 * специализированные методы для работы с командой проектов.</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see TeamMember
 * @see org.springframework.data.jpa.repository.JpaRepository
 */
@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    // ================== ОСНОВНЫЕ МЕТОДЫ ПОИСКА ==================

    /**
     * Находит всех активных членов команды.
     * Используется для отображения команды на сайте.
     *
     * @return список активных членов команды
     */
    List<TeamMember> findByActiveTrue();

    /**
     * Находит всех активных членов команды, отсортированных по порядку сортировки.
     *
     * @return список активных членов команды (по sortOrder)
     */
    List<TeamMember> findByActiveTrueOrderBySortOrderAsc();

    /**
     * Находит всех активных членов команды, отсортированных по имени.
     *
     * @return список активных членов команды (A-Z по имени)
     */
    List<TeamMember> findByActiveTrueOrderByFullNameAsc();

    /**
     * Находит всех неактивных членов команды.
     * Используется в админке для управления командой.
     *
     * @return список неактивных членов команды
     */
    List<TeamMember> findByActiveFalse();

    // ================== ПОИСК ПО ИМЕНИ И ДОЛЖНОСТИ ==================

    /**
     * Находит членов команды по части имени (без учета регистра).
     * Используется для поиска в админке.
     *
     * @param fullName фрагмент имени для поиска
     * @return список найденных членов команды
     */
    List<TeamMember> findByFullNameContainingIgnoreCase(String fullName);

    /**
     * Находит членов команды по части имени (без учета регистра) среди активных.
     *
     * @param fullName фрагмент имени для поиска
     * @return список найденных активных членов команды
     */
    List<TeamMember> findByFullNameContainingIgnoreCaseAndActiveTrue(String fullName);

    /**
     * Находит членов команды по должности (без учета регистра).
     *
     * @param position должность для поиска
     * @return список членов команды с указанной должностью
     */
    List<TeamMember> findByPositionContainingIgnoreCase(String position);

    /**
     * Находит членов команды по должности (без учета регистра) среди активных.
     *
     * @param position должность для поиска
     * @return список активных членов команды с указанной должностью
     */
    List<TeamMember> findByPositionContainingIgnoreCaseAndActiveTrue(String position);

    /**
     * Находит членов команды по части имени или должности (без учета регистра).
     * Комплексный поиск для админки.
     *
     * @param searchTerm поисковый запрос
     * @return список найденных членов команды
     */
    @Query("SELECT tm FROM TeamMember tm WHERE " +
            "LOWER(tm.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(tm.position) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<TeamMember> searchByNameOrPosition(@Param("searchTerm") String searchTerm);

    /**
     * Находит членов команды по части имени или должности (без учета регистра) среди активных.
     *
     * @param searchTerm поисковый запрос
     * @return список найденных активных членов команды
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.active = true AND (" +
            "LOWER(tm.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(tm.position) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<TeamMember> searchActiveByNameOrPosition(@Param("searchTerm") String searchTerm);

    // ================== РАБОТА С ПРОЕКТАМИ ==================

    /**
     * Находит членов команды, участвующих в указанном проекте.
     *
     * @param project проект для поиска
     * @return список членов команды участвующих в проекте
     */
    @Query("SELECT tm FROM TeamMember tm JOIN tm.projects p WHERE p = :project AND tm.active = true")
    List<TeamMember> findByProject(@Param("project") Project project);

    /**
     * Находит членов команды, участвующих в указанном проекте, отсортированных по sortOrder.
     *
     * @param project проект для поиска
     * @return список членов команды участвующих в проекте (по sortOrder)
     */
    @Query("SELECT tm FROM TeamMember tm JOIN tm.projects p WHERE p = :project AND tm.active = true " +
            "ORDER BY tm.sortOrder ASC, tm.fullName ASC")
    List<TeamMember> findByProjectOrderBySortOrder(@Param("project") Project project);

    /**
     * Находит членов команды, участвующих в проекте по ID проекта.
     *
     * @param projectId ID проекта
     * @return список членов команды участвующих в проекте
     */
    @Query("SELECT tm FROM TeamMember tm JOIN tm.projects p WHERE p.id = :projectId AND tm.active = true")
    List<TeamMember> findByProjectId(@Param("projectId") Long projectId);

    /**
     * Находит членов команды, НЕ участвующих в указанном проекте.
     * Используется для добавления участников в проект.
     *
     * @param project проект для исключения
     * @return список членов команды не участвующих в проекте
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.active = true AND tm NOT IN " +
            "(SELECT tm2 FROM TeamMember tm2 JOIN tm2.projects p WHERE p = :project)")
    List<TeamMember> findNotInProject(@Param("project") Project project);

    /**
     * Находит членов команды, не имеющих проектов.
     * Используется для оптимизации команды.
     *
     * @return список членов команды без проектов
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.active = true AND SIZE(tm.projects) = 0")
    List<TeamMember> findWithoutProjects();

    // ================== ПАГИНАЦИЯ ==================

    /**
     * Находит всех активных членов команды с пагинацией.
     *
     * @param pageable объект пагинации
     * @return страница активных членов команды
     */
    Page<TeamMember> findByActiveTrue(Pageable pageable);

    /**
     * Находит всех членов команды с пагинацией.
     *
     * @param pageable объект пагинации
     * @return страница всех членов команды
     */
    Page<TeamMember> findAll(Pageable pageable);

    // ================== СОРТИРОВКА ==================

    /**
     * Находит всех членов команды, отсортированных по имени (A-Z).
     *
     * @return список всех членов команды отсортированных по имени
     */
    List<TeamMember> findAllByOrderByFullNameAsc();

    /**
     * Находит всех членов команды, отсортированных по порядку сортировки.
     *
     * @return список всех членов команды отсортированных по sortOrder
     */
    List<TeamMember> findAllByOrderBySortOrderAsc();

    /**
     * Находит всех членов команды, отсортированных по дате создания (новые сначала).
     *
     * @return список всех членов команды отсортированных по дате создания
     */
    List<TeamMember> findAllByOrderByCreatedAtDesc();

    // ================== СТАТИСТИКА И СВОДНЫЕ ДАННЫЕ ==================

    /**
     * Подсчитывает количество активных членов команды.
     *
     * @return количество активных членов команды
     */
    long countByActiveTrue();

    /**
     * Подсчитывает количество неактивных членов команды.
     *
     * @return количество неактивных членов команды
     */
    long countByActiveFalse();

    /**
     * Находит членов команды с аватаркой.
     * Используется для проверки заполненности данных.
     *
     * @return список членов команды с avatarPath
     */
    List<TeamMember> findByAvatarPathIsNotNull();

    /**
     * Находит членов команды без аватарки.
     * Используется для уведомлений в админке.
     *
     * @return список членов команды без avatarPath
     */
    List<TeamMember> findByAvatarPathIsNull();

    /**
     * Находит членов команды с биографией.
     *
     * @return список членов команды с bio
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.bio IS NOT NULL AND TRIM(tm.bio) != ''")
    List<TeamMember> findWithBio();

    /**
     * Находит членов команды без биографии.
     * Используется для уведомлений в админке.
     *
     * @return список членов команды без bio
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.bio IS NULL OR TRIM(tm.bio) = ''")
    List<TeamMember> findWithoutBio();

    // ================== СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    /**
     * Находит последних N добавленных членов команды.
     * Используется для виджета "Новые участники".
     *
     * @param limit количество членов команды
     * @return список последних добавленных членов команды
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.active = true ORDER BY tm.createdAt DESC")
    List<TeamMember> findRecentTeamMembers(@Param("limit") int limit);

    /**
     * Находит ключевых членов команды (с высокой позицией в сортировке).
     * Используется для выделения руководства.
     *
     * @param limit количество членов команды
     * @return список ключевых членов команды
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.active = true AND tm.sortOrder < 10 ORDER BY tm.sortOrder ASC")
    List<TeamMember> findKeyTeamMembers(@Param("limit") int limit);

    /**
     * Находит членов команды с указанной должностью.
     * Используется для фильтрации по ролям.
     *
     * @param position точная должность
     * @return список членов команды с указанной должностью
     */
    List<TeamMember> findByPosition(String position);

    /**
     * Находит членов команды с указанной должностью среди активных.
     *
     * @param position точная должность
     * @return список активных членов команды с указанной должностью
     */
    List<TeamMember> findByPositionAndActiveTrue(String position);

    /**
     * Проверяет существование члена команды с указанным email.
     *
     * @param email email для проверки
     * @return true если член команды с таким email существует, иначе false
     */
    boolean existsByEmail(String email);

    /**
     * Находит члена команды по email.
     *
     * @param email email для поиска
     * @return Optional содержащий члена команды если найден
     */
    Optional<TeamMember> findByEmail(String email);

    /**
     * Находит членов команды с указанным email среди активных.
     *
     * @param email email для поиска
     * @return Optional содержащего активного члена команды если найден
     */
    Optional<TeamMember> findByEmailAndActiveTrue(String email);
}
