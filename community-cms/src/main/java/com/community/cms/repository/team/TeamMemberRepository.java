package com.community.cms.repository.team;

import com.community.cms.model.team.TeamMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//Описание файла:
//Репозиторий для работы с сущностью TeamMember
//Методы поиска по email, категории, активности
//Фильтрация с пагинацией
//Поиск членов команды по проекту
//Статистические запросы (подсчет по категориям)
//Оптимизированные запросы с JOIN FETCH для загрузки связанных проектов
//Проверка участия в проекте и получение роли
//Методы для главной страницы (ограниченное количество записей)

/**
 * Репозиторий для работы с сущностью {@link TeamMember}.
 * Предоставляет методы для доступа к данным членов команды организации "ЛАДА".
 *
 * @author Vasickin
 * @since 1.0
 */
@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    /**
     * Найти члена команды по email
     *
     * @param email Email члена команды
     * @return Optional с найденным членом команды или пустой Optional
     */
    Optional<TeamMember> findByEmail(String email);

    /**
     * Проверить существование члена команды по email
     *
     * @param email Email члена команды
     * @return true, если член команды с таким email существует
     */
    boolean existsByEmail(String email);

    /**
     * Найти всех членов команды по категории
     *
     * @param category Категория члена команды
     * @return Список членов команды указанной категории
     */
    List<TeamMember> findByCategory(TeamMember.TeamMemberCategory category);

    /**
     * Найти всех членов команды по категории с пагинацией
     *
     * @param category Категория члена команды
     * @param pageable Параметры пагинации
     * @return Страница членов команды указанной категории
     */
    Page<TeamMember> findByCategory(TeamMember.TeamMemberCategory category, Pageable pageable);

    /**
     * Найти активных членов команды
     *
     * @return Список активных членов команды
     */
    List<TeamMember> findByIsActiveTrue();

    /**
     * Найти активных членов команды с пагинацией
     *
     * @param pageable Параметры пагинации
     * @return Страница активных членов команды
     */
    Page<TeamMember> findByIsActiveTrue(Pageable pageable);

    /**
     * Найти членов команды, отображаемых в списке команды
     *
     * @return Список членов команды, которые должны отображаться в списке
     */
    List<TeamMember> findByIsActiveTrueAndShowInTeamListTrue();

    /**
     * Найти членов команды, отображаемых в списке команды, с пагинацией
     *
     * @param pageable Параметры пагинации
     * @return Страница членов команды для отображения в списке
     */
    Page<TeamMember> findByIsActiveTrueAndShowInTeamListTrue(Pageable pageable);

    /**
     * Найти членов команды по фамилии (без учета регистра)
     *
     * @param lastName Фамилия или ее часть
     * @return Список членов команды с указанной фамилией
     */
    List<TeamMember> findByLastNameContainingIgnoreCase(String lastName);

    /**
     * Найти членов команды по должности (без учета регистра)
     *
     * @param position Должность или ее часть
     * @return Список членов команды с указанной должностью
     */
    List<TeamMember> findByPositionContainingIgnoreCase(String position);

    /**
     * Найти членов команды по имени и фамилии (без учета регистра)
     *
     * @param firstName Имя или его часть
     * @param lastName Фамилия или ее часть
     * @return Список членов команды с указанными именем и фамилией
     */
    List<TeamMember> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
            String firstName, String lastName);

    /**
     * Поиск членов команды по нескольким критериям
     *
     * @param category Категория (может быть null)
     * @param search Поисковый запрос (может быть null)
     * @param pageable Параметры пагинации
     * @return Страница членов команды, удовлетворяющих критериям
     */
    @Query("SELECT tm FROM TeamMember tm WHERE " +
            "(:category IS NULL OR tm.category = :category) AND " +
            "tm.isActive = true AND " +
            "(:search IS NULL OR " +
            "LOWER(tm.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(tm.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(tm.middleName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(tm.position) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY tm.category.sortOrder, tm.sortOrder ASC, tm.lastName ASC")
    Page<TeamMember> findByFilters(
            @Param("category") TeamMember.TeamMemberCategory category,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Найти членов команды по ID проекта, в котором они участвуют
     *
     * @param projectId ID проекта
     * @return Список членов команды, участвующих в указанном проекте
     */
    @Query("SELECT tm FROM TeamMember tm JOIN tm.projects p WHERE p.id = :projectId")
    List<TeamMember> findByProjectId(@Param("projectId") Long projectId);

    /**
     * Найти членов команды по ID проекта с пагинацией
     *
     * @param projectId ID проекта
     * @param pageable Параметры пагинации
     * @return Страница членов команды, участвующих в указанном проекте
     */
    @Query("SELECT tm FROM TeamMember tm JOIN tm.projects p WHERE p.id = :projectId")
    Page<TeamMember> findByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    /**
     * Найти членов команды, у которых есть аватар
     *
     * @return Список членов команды с загруженным аватаром
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.avatar IS NOT NULL")
    List<TeamMember> findWithAvatar();

    /**
     * Найти членов команды, у которых есть указанный проект в списке проектов
     *
     * @param projectId ID проекта
     * @return Список членов команды, связанных с указанным проектом
     */
    @Query("SELECT DISTINCT tm FROM TeamMember tm JOIN tm.projects p WHERE p.id = :projectId")
    List<TeamMember> findMembersByProjectId(@Param("projectId") Long projectId);

    /**
     * Найти членов команды по списку ID
     *
     * @param ids Список ID членов команды
     * @return Список членов команды с указанными ID
     */
    List<TeamMember> findByIdIn(List<Long> ids);

    /**
     * Получить всех членов команды, отсортированных по категории и порядку
     *
     * @return Список членов команды, отсортированный по категории и порядку
     */
    @Query("SELECT tm FROM TeamMember tm WHERE tm.isActive = true " +
            "ORDER BY tm.category.sortOrder, tm.sortOrder ASC, tm.lastName ASC, tm.firstName ASC")
    List<TeamMember> findAllActiveOrdered();

    /**
     * Получить членов команды для главной страницы (ограниченное количество)
     *
     * @param limit Максимальное количество записей
     * @return Список членов команды для главной страницы
     */
    @Query(value = "SELECT * FROM team_members tm " +
            "WHERE tm.is_active = true AND tm.show_in_team_list = true " +
            "ORDER BY tm.category, tm.sort_order, tm.last_name, tm.first_name " +
            "LIMIT :limit", nativeQuery = true)
    List<TeamMember> findForHomepage(@Param("limit") int limit);

    /**
     * Получить количество членов команды по категории
     *
     * @param category Категория члена команды
     * @return Количество членов команды указанной категории
     */
    long countByCategory(TeamMember.TeamMemberCategory category);

    /**
     * Получить общее количество активных членов команды
     *
     * @return Количество активных членов команды
     */
    long countByIsActiveTrue();

    /**
     * Получить количество членов команды, отображаемых в списке
     *
     * @return Количество членов команды для отображения в списке
     */
    long countByIsActiveTrueAndShowInTeamListTrue();

    /**
     * Найти членов команды по ID с полной загрузкой связанных проектов
     *
     * @param id ID члена команды
     * @return Optional с найденным членом команды или пустой Optional
     */
    @Query("SELECT tm FROM TeamMember tm LEFT JOIN FETCH tm.projects WHERE tm.id = :id")
    Optional<TeamMember> findByIdWithProjects(@Param("id") Long id);

    /**
     * Найти всех членов команды с загрузкой проектов
     *
     * @return Список членов команды с загруженными проектами
     */
    @Query("SELECT DISTINCT tm FROM TeamMember tm LEFT JOIN FETCH tm.projects")
    List<TeamMember> findAllWithProjects();

    /**
     * Найти членов команды по категории с загрузкой проектов
     *
     * @param category Категория члена команды
     * @return Список членов команды указанной категории с загруженными проектами
     */
    @Query("SELECT DISTINCT tm FROM TeamMember tm LEFT JOIN FETCH tm.projects WHERE tm.category = :category")
    List<TeamMember> findByCategoryWithProjects(@Param("category") TeamMember.TeamMemberCategory category);

    /**
     * Проверить, участвует ли член команды в указанном проекте
     *
     * @param teamMemberId ID члена команды
     * @param projectId ID проекта
     * @return true, если член команды участвует в проекте
     */
    @Query("SELECT COUNT(tm) > 0 FROM TeamMember tm JOIN tm.projects p " +
            "WHERE tm.id = :teamMemberId AND p.id = :projectId")
    boolean isMemberInProject(@Param("teamMemberId") Long teamMemberId,
                              @Param("projectId") Long projectId);

    /**
     * Получить роль члена команды в конкретном проекте
     *
     * @param teamMemberId ID члена команды
     * @param projectId ID проекта
     * @return Роль в проекте или null, если член команды не участвует в проекте
     */
    @Query("SELECT VALUE(tm.projectRoles) FROM TeamMember tm JOIN tm.projects p " +
            "WHERE tm.id = :teamMemberId AND p.id = :projectId")
    String findRoleInProject(@Param("teamMemberId") Long teamMemberId,
                             @Param("projectId") Long projectId);
}
