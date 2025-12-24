package com.community.cms.service.project;

import com.community.cms.domain.model.content.Project;
import com.community.cms.model.project.TeamMember;
import com.community.cms.repository.project.TeamMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления членами команды организации "ЛАДА".
 *
 * <p>Предоставляет бизнес-логику для работы с членами команды,
 * включая управление участием в проектах и ролями.</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see TeamMember
 * @see TeamMemberRepository
 */
@Service
@Transactional
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;

    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param teamMemberRepository репозиторий для работы с членами команды
     */
    @Autowired
    public TeamMemberService(TeamMemberRepository teamMemberRepository) {
        this.teamMemberRepository = teamMemberRepository;
    }

    // ================== CRUD ОПЕРАЦИИ ==================

    /**
     * Сохраняет члена команды.
     *
     * @param teamMember член команды для сохранения
     * @return сохраненный член команды
     */
    public TeamMember save(TeamMember teamMember) {
        validateTeamMember(teamMember);
        return teamMemberRepository.save(teamMember);
    }

    /**
     * Обновляет существующего члена команды.
     *
     * @param teamMember член команды для обновления
     * @return обновленный член команды
     */
    public TeamMember update(TeamMember teamMember) {
        validateTeamMember(teamMember);
        return teamMemberRepository.save(teamMember);
    }

    /**
     * Находит члена команды по ID.
     *
     * @param id идентификатор члена команды
     * @return Optional с членом команды, если найден
     */
    @Transactional(readOnly = true)
    public Optional<TeamMember> findById(Long id) {
        return teamMemberRepository.findById(id);
    }

    /**
     * Удаляет члена команды по ID.
     *
     * @param id идентификатор члена команды для удаления
     */
    public void deleteById(Long id) {
        teamMemberRepository.deleteById(id);
    }

    // ================== ПОИСК ПО АКТИВНОСТИ ==================

    /**
     * Находит всех активных членов команды.
     *
     * @return список активных членов команды
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findAllActive() {
        return teamMemberRepository.findByActiveTrue();
    }

    /**
     * Находит всех активных членов команды, отсортированных по порядку сортировки.
     *
     * @return список активных членов команды (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findAllActiveOrderBySortOrder() {
        return teamMemberRepository.findByActiveTrueOrderBySortOrderAsc();
    }

    /**
     * Находит всех активных членов команды, отсортированных по имени.
     *
     * @return список активных членов команды (по имени)
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findAllActiveOrderByName() {
        return teamMemberRepository.findByActiveTrueOrderByFullNameAsc();
    }

    /**
     * Находит всех неактивных членов команды.
     *
     * @return список неактивных членов команды
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findAllInactive() {
        return teamMemberRepository.findByActiveFalse();
    }

    // ================== ПОИСК ПО ИМЕНИ И ДОЛЖНОСТИ ==================

    /**
     * Находит членов команды по части имени (без учета регистра).
     *
     * @param fullName фрагмент имени для поиска
     * @return список найденных членов команды
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findByNameContaining(String fullName) {
        return teamMemberRepository.findByFullNameContainingIgnoreCase(fullName);
    }

    /**
     * Находит активных членов команды по части имени (без учета регистра).
     *
     * @param fullName фрагмент имени для поиска
     * @return список найденных активных членов команды
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findActiveByNameContaining(String fullName) {
        return teamMemberRepository.findByFullNameContainingIgnoreCaseAndActiveTrue(fullName);
    }

    /**
     * Находит членов команды по должности (без учета регистра).
     *
     * @param position должность для поиска
     * @return список членов команды с указанной должностью
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findByPositionContaining(String position) {
        return teamMemberRepository.findByPositionContainingIgnoreCase(position);
    }

    /**
     * Находит активных членов команды по должности (без учета регистра).
     *
     * @param position должность для поиска
     * @return список активных членов команды с указанной должностью
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findActiveByPositionContaining(String position) {
        return teamMemberRepository.findByPositionContainingIgnoreCaseAndActiveTrue(position);
    }

    /**
     * Комплексный поиск членов команды по имени или должности (без учета регистра).
     *
     * @param searchTerm поисковый запрос
     * @return список найденных членов команды
     */
    @Transactional(readOnly = true)
    public List<TeamMember> searchByNameOrPosition(String searchTerm) {
        return teamMemberRepository.searchByNameOrPosition(searchTerm);
    }

    /**
     * Комплексный поиск активных членов команды по имени или должности.
     *
     * @param searchTerm поисковый запрос
     * @return список найденных активных членов команды
     */
    @Transactional(readOnly = true)
    public List<TeamMember> searchActiveByNameOrPosition(String searchTerm) {
        return teamMemberRepository.searchActiveByNameOrPosition(searchTerm);
    }

    // ================== РАБОТА С ПРОЕКТАМИ ==================

    /**
     * Находит членов команды, участвующих в указанном проекте.
     *
     * @param project проект
     * @return список членов команды участвующих в проекте
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findByProject(Project project) {
        return teamMemberRepository.findByProject(project);
    }

    /**
     * Находит членов команды, участвующих в указанном проекте, отсортированных по порядку сортировки.
     *
     * @param project проект
     * @return список членов команды участвующих в проекте (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findByProjectOrderBySortOrder(Project project) {
        return teamMemberRepository.findByProjectOrderBySortOrder(project);
    }

    /**
     * Находит членов команды, участвующих в проекте по ID проекта.
     *
     * @param projectId ID проекта
     * @return список членов команды участвующих в проекте
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findByProjectId(Long projectId) {
        return teamMemberRepository.findByProjectId(projectId);
    }

    /**
     * Находит членов команды, НЕ участвующих в указанном проекте.
     *
     * @param project проект
     * @return список членов команды не участвующих в проекте
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findNotInProject(Project project) {
        return teamMemberRepository.findNotInProject(project);
    }

    /**
     * Находит членов команды, не имеющих проектов.
     *
     * @return список членов команды без проектов
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findWithoutProjects() {
        return teamMemberRepository.findWithoutProjects();
    }

    // ================== ПАГИНАЦИЯ ==================

    /**
     * Находит всех активных членов команды с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница активных членов команды
     */
    @Transactional(readOnly = true)
    public Page<TeamMember> findAllActive(Pageable pageable) {
        return teamMemberRepository.findByActiveTrue(pageable);
    }

    /**
     * Находит всех членов команды с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница всех членов команды
     */
    @Transactional(readOnly = true)
    public Page<TeamMember> findAll(Pageable pageable) {
        return teamMemberRepository.findAll(pageable);
    }

    // ================== СОРТИРОВКА ==================

    /**
     * Находит всех членов команды, отсортированных по имени (A-Z).
     *
     * @return список всех членов команды отсортированных по имени
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findAllOrderByName() {
        return teamMemberRepository.findAllByOrderByFullNameAsc();
    }

    /**
     * Находит всех членов команды, отсортированных по порядку сортировки.
     *
     * @return список всех членов команды отсортированных по sortOrder
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findAllOrderBySortOrder() {
        return teamMemberRepository.findAllByOrderBySortOrderAsc();
    }

    /**
     * Находит всех членов команды, отсортированных по дате создания (новые сначала).
     *
     * @return список всех членов команды отсортированных по дате создания
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findAllOrderByCreatedDateDesc() {
        return teamMemberRepository.findAllByOrderByCreatedAtDesc();
    }

    // ================== СТАТИСТИКА И СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    /**
     * Подсчитывает количество активных членов команды.
     *
     * @return количество активных членов команды
     */
    @Transactional(readOnly = true)
    public long countActive() {
        return teamMemberRepository.countByActiveTrue();
    }

    /**
     * Подсчитывает количество неактивных членов команды.
     *
     * @return количество неактивных членов команды
     */
    @Transactional(readOnly = true)
    public long countInactive() {
        return teamMemberRepository.countByActiveFalse();
    }

    /**
     * Находит членов команды с аватаркой.
     *
     * @return список членов команды с аватаркой
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findWithAvatar() {
        return teamMemberRepository.findByAvatarPathIsNotNull();
    }

    /**
     * Находит членов команды без аватарки.
     *
     * @return список членов команды без аватарки
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findWithoutAvatar() {
        return teamMemberRepository.findByAvatarPathIsNull();
    }

    /**
     * Находит членов команды с биографией.
     *
     * @return список членов команды с биографией
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findWithBio() {
        return teamMemberRepository.findWithBio();
    }

    /**
     * Находит членов команды без биографии.
     *
     * @return список членов команды без биографии
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findWithoutBio() {
        return teamMemberRepository.findWithoutBio();
    }

    /**
     * Находит последних N добавленных членов команды.
     *
     * @param limit количество членов команды
     * @return список последних добавленных членов команды
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findRecentTeamMembers(int limit) {
        return teamMemberRepository.findRecentTeamMembers(limit);
    }

    /**
     * Находит ключевых членов команды (с высокой позицией в сортировке).
     *
     * @param limit количество членов команды
     * @return список ключевых членов команды
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findKeyTeamMembers(int limit) {
        return teamMemberRepository.findKeyTeamMembers(limit);
    }

    /**
     * Находит членов команды с указанной должностью.
     *
     * @param position точная должность
     * @return список членов команды с указанной должностью
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findByExactPosition(String position) {
        return teamMemberRepository.findByPosition(position);
    }

    /**
     * Находит активных членов команды с указанной должностью.
     *
     * @param position точная должность
     * @return список активных членов команды с указанной должностью
     */
    @Transactional(readOnly = true)
    public List<TeamMember> findActiveByExactPosition(String position) {
        return teamMemberRepository.findByPositionAndActiveTrue(position);
    }

    // ================== РАБОТА С EMAIL ==================

    /**
     * Проверяет существование члена команды с указанным email.
     *
     * @param email email для проверки
     * @return true если член команды с таким email существует, иначе false
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return teamMemberRepository.existsByEmail(email);
    }

    /**
     * Находит члена команды по email.
     *
     * @param email email для поиска
     * @return Optional с членом команды, если найден
     */
    @Transactional(readOnly = true)
    public Optional<TeamMember> findByEmail(String email) {
        return teamMemberRepository.findByEmail(email);
    }

    /**
     * Находит активного члена команды по email.
     *
     * @param email email для поиска
     * @return Optional с активным членом команды, если найден
     */
    @Transactional(readOnly = true)
    public Optional<TeamMember> findActiveByEmail(String email) {
        return teamMemberRepository.findByEmailAndActiveTrue(email);
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Проверяет бизнес-правила для члена команды.
     *
     * @param teamMember член команды для валидации
     * @throws IllegalArgumentException если член команды невалиден
     */
    private void validateTeamMember(TeamMember teamMember) {
        if (teamMember == null) {
            throw new IllegalArgumentException("Член команды не может быть null");
        }

        if (teamMember.getFullName() == null || teamMember.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Полное имя члена команды обязательно");
        }

        if (teamMember.getPosition() == null || teamMember.getPosition().trim().isEmpty()) {
            throw new IllegalArgumentException("Должность члена команды обязательна");
        }

        if (teamMember.getSortOrder() == null) {
            teamMember.setSortOrder(0);
        }
    }

    /**
     * Создает нового члена команды.
     *
     * @param fullName полное имя
     * @param position должность
     * @param bio биография (опционально)
     * @param avatarPath путь к аватарке (опционально)
     * @param email email (опционально)
     * @param phone телефон (опционально)
     * @param socialLinks ссылки на соцсети в формате JSON (опционально)
     * @param sortOrder порядок сортировки
     * @return созданный член команды
     */
    public TeamMember createTeamMember(String fullName, String position,
                                       String bio, String avatarPath,
                                       String email, String phone,
                                       String socialLinks, Integer sortOrder) {
        TeamMember teamMember = new TeamMember(fullName, position);
        teamMember.setBio(bio);
        teamMember.setAvatarPath(avatarPath);
        teamMember.setEmail(email);
        teamMember.setPhone(phone);
        teamMember.setSocialLinks(socialLinks);
        teamMember.setSortOrder(sortOrder != null ? sortOrder : 0);
        teamMember.setActive(true);

        return save(teamMember);
    }

    /**
     * Активирует члена команды.
     *
     * @param teamMember член команды для активации
     * @return активированный член команды
     */
    public TeamMember activate(TeamMember teamMember) {
        teamMember.setActive(true);
        return teamMemberRepository.save(teamMember);
    }

    /**
     * Деактивирует члена команды.
     *
     * @param teamMember член команды для деактивации
     * @return деактивированный член команды
     */
    public TeamMember deactivate(TeamMember teamMember) {
        teamMember.setActive(false);
        return teamMemberRepository.save(teamMember);
    }

    /**
     * Активирует члена команды по ID.
     *
     * @param id ID члена команды
     * @return активированный член команды
     * @throws IllegalArgumentException если член команды не найден
     */
    public TeamMember activateById(Long id) {
        TeamMember teamMember = teamMemberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Член команды с ID " + id + " не найден"));
        return activate(teamMember);
    }

    /**
     * Деактивирует члена команды по ID.
     *
     * @param id ID члена команды
     * @return деактивированный член команды
     * @throws IllegalArgumentException если член команды не найден
     */
    public TeamMember deactivateById(Long id) {
        TeamMember teamMember = teamMemberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Член команды с ID " + id + " не найден"));
        return deactivate(teamMember);
    }

    /**
     * Добавляет проект к члену команды.
     *
     * @param teamMember член команды
     * @param project проект для добавления
     * @return обновленный член команды
     */
    public TeamMember addProjectToTeamMember(TeamMember teamMember, Project project) {
        teamMember.addProject(project);
        return teamMemberRepository.save(teamMember);
    }

    /**
     * Удаляет проект у члена команды.
     *
     * @param teamMember член команды
     * @param project проект для удаления
     * @return обновленный член команды
     */
    public TeamMember removeProjectFromTeamMember(TeamMember teamMember, Project project) {
        teamMember.removeProject(project);
        return teamMemberRepository.save(teamMember);
    }

    /**
     * Устанавливает роль члена команды в конкретном проекте.
     *
     * @param teamMember член команды
     * @param projectId ID проекта
     * @param role роль в проекте
     * @return обновленный член команды
     */
    public TeamMember setRoleForProject(TeamMember teamMember, Long projectId, String role) {
        teamMember.setRoleForProject(projectId, role);
        return teamMemberRepository.save(teamMember);
    }

    /**
     * Получает роль члена команды в конкретном проекте.
     *
     * @param teamMember член команды
     * @param projectId ID проекта
     * @return роль в проекте или null если не установлена
     */
    public String getRoleForProject(TeamMember teamMember, Long projectId) {
        return teamMember.getRoleForProject(projectId);
    }

    /**
     * Удаляет роль члена команды из проекта.
     *
     * @param teamMember член команды
     * @param projectId ID проекта
     * @return удаленная роль или null если не найдена
     */
    public String removeRoleForProject(TeamMember teamMember, Long projectId) {
        String removedRole = teamMember.removeRoleForProject(projectId);
        teamMemberRepository.save(teamMember);
        return removedRole;
    }

    /**
     * Проверяет имеет ли член команды аватарку.
     *
     * @param teamMember член команды
     * @return true если у члена команды есть аватарка, иначе false
     */
    public boolean hasAvatar(TeamMember teamMember) {
        return teamMember.hasAvatar();
    }

    /**
     * Проверяет имеет ли член команды биографию.
     *
     * @param teamMember член команды
     * @return true если у члена команды есть биография, иначе false
     */
    public boolean hasBio(TeamMember teamMember) {
        return teamMember.hasBio();
    }

    /**
     * Получает количество проектов, в которых участвует член команды.
     *
     * @param teamMember член команды
     * @return количество проектов
     */
    public int getProjectsCount(TeamMember teamMember) {
        return teamMember.getProjectsCount();
    }

    /**
     * Получает инициалы члена команды (для аватарок по умолчанию).
     *
     * @param teamMember член команды
     * @return инициалы (2 буквы)
     */
    public String getInitials(TeamMember teamMember) {
        return teamMember.getInitials();
    }

    /**
     * Получает отображаемую роль члена команды для проекта.
     *
     * @param teamMember член команды
     * @param projectId ID проекта
     * @return роль для отображения
     */
    public String getDisplayRoleForProject(TeamMember teamMember, Long projectId) {
        return teamMember.getDisplayRoleForProject(projectId);
    }

    /**
     * Проверяет участвует ли член команды в указанном проекте.
     *
     * @param teamMember член команды
     * @param project проект для проверки
     * @return true если участвует, иначе false
     */
    public boolean participatesInProject(TeamMember teamMember, Project project) {
        return teamMember.participatesInProject(project);
    }

    /**
     * Проверяет участвует ли член команды в проекте по ID.
     *
     * @param teamMember член команды
     * @param projectId ID проекта для проверки
     * @return true если участвует, иначе false
     */
    public boolean participatesInProject(TeamMember teamMember, Long projectId) {
        return teamMember.participatesInProject(projectId);
    }

    /**
     * Обновляет порядок сортировки членов команды.
     *
     * @param teamMembers список членов команды с обновленными sortOrder
     * @return список обновленных членов команды
     */
    public List<TeamMember> updateSortOrder(List<TeamMember> teamMembers) {
        return teamMemberRepository.saveAll(teamMembers);
    }
}
