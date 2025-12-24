package com.community.cms.service.project;

import com.community.cms.domain.model.people.Partner;
import com.community.cms.domain.model.content.Project;
import com.community.cms.domain.model.people.Partner.PartnerType;
import com.community.cms.repository.project.ProjectPartnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления партнерами проектов.
 *
 * <p>Предоставляет бизнес-логику для работы с партнерами проектов,
 * включая спонсоров, информационных партнеров и других участников.</p>
 *
 * @author Community CMS
 * @version 1.0
 * @since 2025
 * @see Partner
 * @see ProjectPartnerRepository
 */
@Service
@Transactional
public class ProjectPartnerService {

    private final ProjectPartnerRepository projectPartnerRepository;

    /**
     * Конструктор с инъекцией зависимостей.
     *
     * @param projectPartnerRepository репозиторий для работы с партнерами проектов
     */
    @Autowired
    public ProjectPartnerService(ProjectPartnerRepository projectPartnerRepository) {
        this.projectPartnerRepository = projectPartnerRepository;
    }

    // ================== CRUD ОПЕРАЦИИ ==================

    /**
     * Сохраняет партнера проекта.
     *
     * @param projectPartner партнер проекта для сохранения
     * @return сохраненный партнер проекта
     */
    public Partner save(Partner projectPartner) {
        validateProjectPartner(projectPartner);
        return projectPartnerRepository.save(projectPartner);
    }

    /**
     * Обновляет существующего партнера проекта.
     *
     * @param projectPartner партнер проекта для обновления
     * @return обновленный партнер проекта
     */
    public Partner update(Partner projectPartner) {
        validateProjectPartner(projectPartner);
        return projectPartnerRepository.save(projectPartner);
    }

    /**
     * Находит партнера проекта по ID.
     *
     * @param id идентификатор партнера проекта
     * @return Optional с партнером проекта, если найден
     */
    @Transactional(readOnly = true)
    public Optional<Partner> findById(Long id) {
        return projectPartnerRepository.findById(id);
    }

    /**
     * Удаляет партнера проекта по ID.
     *
     * @param id идентификатор партнера проекта для удаления
     */
    public void deleteById(Long id) {
        projectPartnerRepository.deleteById(id);
    }

    // ================== ПОИСК ПО ПРОЕКТУ ==================

    /**
     * Находит всех партнеров указанного проекта.
     *
     * @param project проект
     * @return список партнеров проекта
     */
    @Transactional(readOnly = true)
    public List<Partner> findByProject(Project project) {
        return projectPartnerRepository.findByProject(project);
    }

    /**
     * Находит всех партнеров проекта по ID проекта.
     * ТРЕБУЕТСЯ РЕАЛИЗАЦИЯ В РЕПОЗИТОРИИ.
     *
     * @param projectId ID проекта
     * @return список партнеров проекта
     * @throws UnsupportedOperationException пока не реализовано
     */
    @Transactional(readOnly = true)
    public List<Partner> findByProjectId(Long projectId) {
        // Для работы этого метода нужно добавить в ProjectPartnerRepository:
        // @Query("SELECT pp FROM Partner pp WHERE pp.project.id = :projectId")
        // List<Partner> findByProjectId(@Param("projectId") Long projectId);

        throw new UnsupportedOperationException(
                "Метод findByProjectId требует реализации в ProjectPartnerRepository. " +
                        "Добавьте: @Query(\"SELECT pp FROM Partner pp WHERE pp.project.id = :projectId\") " +
                        "List<Partner> findByProjectId(@Param(\"projectId\") Long projectId);"
        );
    }

    /**
     * Находит всех партнеров проекта, отсортированных по порядку сортировки.
     *
     * @param project проект
     * @return список партнеров проекта (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<Partner> findByProjectOrderBySortOrder(Project project) {
        return projectPartnerRepository.findByProjectOrderBySortOrderAsc(project);
    }

    /**
     * Находит всех партнеров проекта, отсортированных по названию.
     *
     * @param project проект
     * @return список партнеров проекта (по названию)
     */
    @Transactional(readOnly = true)
    public List<Partner> findByProjectOrderByName(Project project) {
        return projectPartnerRepository.findByProjectOrderByNameAsc(project);
    }

    // ================== ПОИСК ПО ТИПУ ПАРТНЕРСТВА ==================

    /**
     * Находит партнеров проекта по типу партнерства.
     *
     * @param project проект
     * @param partnerType тип партнерства
     * @return список партнеров проекта указанного типа
     */
    @Transactional(readOnly = true)
    public List<Partner> findByProjectAndPartnerType(Project project, PartnerType partnerType) {
        return projectPartnerRepository.findByProjectAndPartnerType(project, partnerType);
    }

    /**
     * Находит партнеров проекта по типу партнерства, отсортированных по порядку сортировки.
     *
     * @param project проект
     * @param partnerType тип партнерства
     * @return список партнеров проекта указанного типа (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<Partner> findByProjectAndPartnerTypeOrderBySortOrder(Project project, PartnerType partnerType) {
        return projectPartnerRepository.findByProjectAndPartnerTypeOrderBySortOrderAsc(project, partnerType);
    }

    // ================== ПОИСК ПО АКТИВНОСТИ ==================

    /**
     * Находит активных партнеров проекта.
     *
     * @param project проект
     * @return список активных партнеров проекта
     */
    @Transactional(readOnly = true)
    public List<Partner> findActiveByProject(Project project) {
        return projectPartnerRepository.findByProjectAndActiveTrue(project);
    }

    /**
     * Находит активных партнеров проекта, отсортированных по порядку сортировки.
     *
     * @param project проект
     * @return список активных партнеров проекта (по sortOrder)
     */
    @Transactional(readOnly = true)
    public List<Partner> findActiveByProjectOrderBySortOrder(Project project) {
        return projectPartnerRepository.findByProjectAndActiveTrueOrderBySortOrderAsc(project);
    }

    /**
     * Находит неактивных партнеров проекта.
     *
     * @param project проект
     * @return список неактивных партнеров проекта
     */
    @Transactional(readOnly = true)
    public List<Partner> findInactiveByProject(Project project) {
        return projectPartnerRepository.findByProjectAndActiveFalse(project);
    }

    // ================== ПОИСК ПО НАЗВАНИЮ И КОНТАКТАМ ==================

    /**
     * Находит партнеров по части названия (без учета регистра).
     *
     * @param name фрагмент названия для поиска
     * @return список найденных партнеров
     */
    @Transactional(readOnly = true)
    public List<Partner> findByNameContaining(String name) {
        return projectPartnerRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Находит активных партнеров по части названия (без учета регистра).
     *
     * @param name фрагмент названия для поиска
     * @return список найденных активных партнеров
     */
    @Transactional(readOnly = true)
    public List<Partner> findActiveByNameContaining(String name) {
        return projectPartnerRepository.findByNameContainingIgnoreCaseAndActiveTrue(name);
    }

    /**
     * Находит партнеров по email.
     *
     * @param email email для поиска
     * @return список партнеров с указанным email
     */
    @Transactional(readOnly = true)
    public List<Partner> findByContactEmail(String email) {
        return projectPartnerRepository.findByContactEmail(email);
    }

    /**
     * Находит партнеров по контактному лицу (без учета регистра).
     *
     * @param contactPerson фрагмент имени контактного лица для поиска
     * @return список найденных партнеров
     */
    @Transactional(readOnly = true)
    public List<Partner> findByContactPersonContaining(String contactPerson) {
        return projectPartnerRepository.findByContactPersonContainingIgnoreCase(contactPerson);
    }

    // ================== ПОИСК ПО ЛОГОТИПУ И САЙТУ ==================

    /**
     * Находит партнеров с логотипом.
     *
     * @return список партнеров с логотипом
     */
    @Transactional(readOnly = true)
    public List<Partner> findWithLogo() {
        return projectPartnerRepository.findByLogoPathIsNotNull();
    }

    /**
     * Находит партнеров без логотипа.
     *
     * @return список партнеров без логотипа
     */
    @Transactional(readOnly = true)
    public List<Partner> findWithoutLogo() {
        return projectPartnerRepository.findByLogoPathIsNull();
    }

    /**
     * Находит партнеров с сайтом.
     *
     * @return список партнеров с сайтом
     */
    @Transactional(readOnly = true)
    public List<Partner> findWithWebsite() {
        return projectPartnerRepository.findByWebsiteUrlIsNotNull();
    }

    /**
     * Находит партнеров без сайта.
     *
     * @return список партнеров без сайта
     */
    @Transactional(readOnly = true)
    public List<Partner> findWithoutWebsite() {
        return projectPartnerRepository.findByWebsiteUrlIsNull();
    }

    /**
     * Проверяет существование партнера с указанным URL сайта.
     *
     * @param websiteUrl URL сайта для проверки
     * @return true если партнер с таким сайтом существует, иначе false
     */
    @Transactional(readOnly = true)
    public boolean existsByWebsiteUrl(String websiteUrl) {
        return projectPartnerRepository.existsByWebsiteUrl(websiteUrl);
    }

    /**
     * Проверяет существование партнера с указанным email.
     *
     * @param email email для проверки
     * @return true если партнер с таким email существует, иначе false
     */
    @Transactional(readOnly = true)
    public boolean existsByContactEmail(String email) {
        return projectPartnerRepository.existsByContactEmail(email);
    }

    // ================== СПЕЦИАЛЬНЫЕ ЗАПРОСЫ ==================

    /**
     * Находит спонсоров проекта.
     *
     * @param project проект
     * @return список спонсоров проекта
     */
    @Transactional(readOnly = true)
    public List<Partner> findSponsorsByProject(Project project) {
        return projectPartnerRepository.findSponsorsByProject(project);
    }

    /**
     * Находит информационных партнеров проекта.
     *
     * @param project проект
     * @return список информационных партнеров проекта
     */
    @Transactional(readOnly = true)
    public List<Partner> findInformationPartnersByProject(Project project) {
        return projectPartnerRepository.findInformationPartnersByProject(project);
    }

    /**
     * Находит первые N активных партнеров проекта.
     *
     * @param project проект
     * @param limit количество партнеров
     * @return список первых N активных партнеров проекта
     */
    @Transactional(readOnly = true)
    public List<Partner> findFirstNActiveByProject(Project project, int limit) {
        return projectPartnerRepository.findFirstNByProject(project, limit);
    }

    /**
     * Находит партнеров проекта без описания.
     *
     * @param project проект
     * @return список партнеров проекта без описания
     */
    @Transactional(readOnly = true)
    public List<Partner> findWithoutDescriptionByProject(Project project) {
        return projectPartnerRepository.findWithoutDescriptionByProject(project);
    }

    /**
     * Находит партнеров проекта без контактной информации.
     *
     * @param project проект
     * @return список партнеров проекта без контактной информации
     */
    @Transactional(readOnly = true)
    public List<Partner> findWithoutContactInfoByProject(Project project) {
        return projectPartnerRepository.findWithoutContactInfoByProject(project);
    }

    /**
     * Находит партнеров, участвующих в нескольких проектах.
     *
     * @param minProjects минимальное количество проектов
     * @return список партнеров, участвующих в minProjects и более проектах
     */
    @Transactional(readOnly = true)
    public List<Partner> findPartnersInMultipleProjects(int minProjects) {
        return projectPartnerRepository.findPartnersInMultipleProjects(minProjects);
    }

    // ================== СТАТИСТИКА ==================

    /**
     * Подсчитывает количество партнеров проекта.
     *
     * @param project проект
     * @return количество партнеров проекта
     */
    @Transactional(readOnly = true)
    public long countByProject(Project project) {
        return projectPartnerRepository.countByProject(project);
    }

    /**
     * Подсчитывает количество активных партнеров проекта.
     *
     * @param project проект
     * @return количество активных партнеров проекта
     */
    @Transactional(readOnly = true)
    public long countActiveByProject(Project project) {
        return projectPartnerRepository.countByProjectAndActiveTrue(project);
    }

    /**
     * Подсчитывает количество партнеров проекта по типу партнерства.
     *
     * @param project проект
     * @param partnerType тип партнерства
     * @return количество партнеров проекта указанного типа
     */
    @Transactional(readOnly = true)
    public long countByProjectAndPartnerType(Project project, PartnerType partnerType) {
        return projectPartnerRepository.countByProjectAndPartnerType(project, partnerType);
    }

    /**
     * Находит все уникальные типы партнерства проекта.
     *
     * @param project проект
     * @return список уникальных типов партнерства проекта
     */
    @Transactional(readOnly = true)
    public List<PartnerType> findDistinctPartnerTypesByProject(Project project) {
        return projectPartnerRepository.findDistinctPartnerTypesByProject(project);
    }

    // ================== УДАЛЕНИЕ ПО СВЯЗЯМ ==================

    /**
     * Удаляет всех партнеров указанного проекта.
     *
     * @param project проект
     */
    public void deleteByProject(Project project) {
        projectPartnerRepository.deleteByProject(project);
    }

    /**
     * Удаляет партнеров проекта по типу партнерства.
     *
     * @param project проект
     * @param partnerType тип партнерства
     */
    public void deleteByProjectAndPartnerType(Project project, PartnerType partnerType) {
        projectPartnerRepository.deleteByProjectAndPartnerType(project, partnerType);
    }

    // ================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==================

    /**
     * Проверяет бизнес-правила для партнера проекта.
     *
     * @param projectPartner партнер проекта для валидации
     * @throws IllegalArgumentException если партнер проекта невалиден
     */
    private void validateProjectPartner(Partner projectPartner) {
        if (projectPartner == null) {
            throw new IllegalArgumentException("Партнер проекта не может быть null");
        }

        if (projectPartner.getProject() == null) {
            throw new IllegalArgumentException("Партнер должен быть привязан к проекту");
        }

        if (projectPartner.getName() == null || projectPartner.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название партнера обязательно");
        }

        if (projectPartner.getPartnerType() == null) {
            projectPartner.setPartnerType(PartnerType.OTHER);
        }

        if (projectPartner.getSortOrder() == null) {
            projectPartner.setSortOrder(0);
        }
    }

    /**
     * Создает нового партнера проекта.
     *
     * @param project проект
     * @param name название партнера
     * @param partnerType тип партнерства
     * @param description описание (опционально)
     * @param logoPath путь к логотипу (опционально)
     * @param websiteUrl URL сайта (опционально)
     * @param contactEmail контактный email (опционально)
     * @param contactPhone контактный телефон (опционально)
     * @param contactPerson контактное лицо (опционально)
     * @param sortOrder порядок сортировки
     * @return созданный партнер проекта
     */
    public Partner createProjectPartner(Project project, String name, PartnerType partnerType,
                                        String description, String logoPath, String websiteUrl,
                                        String contactEmail, String contactPhone, String contactPerson,
                                        Integer sortOrder) {
        Partner projectPartner = new Partner(project, name, partnerType);
        projectPartner.setDescription(description);
        projectPartner.setLogoPath(logoPath);
        projectPartner.setWebsiteUrl(websiteUrl);
        projectPartner.setContactEmail(contactEmail);
        projectPartner.setContactPhone(contactPhone);
        projectPartner.setContactPerson(contactPerson);
        projectPartner.setSortOrder(sortOrder != null ? sortOrder : 0);
        projectPartner.setActive(true);

        return save(projectPartner);
    }

    /**
     * Активирует партнера проекта.
     *
     * @param projectPartner партнер для активации
     * @return активированный партнер проекта
     */
    public Partner activate(Partner projectPartner) {
        projectPartner.setActive(true);
        return projectPartnerRepository.save(projectPartner);
    }

    /**
     * Деактивирует партнера проекта.
     *
     * @param projectPartner партнер для деактивации
     * @return деактивированный партнер проекта
     */
    public Partner deactivate(Partner projectPartner) {
        projectPartner.setActive(false);
        return projectPartnerRepository.save(projectPartner);
    }

    /**
     * Активирует партнера проекта по ID.
     *
     * @param id ID партнера
     * @return активированный партнер проекта
     * @throws IllegalArgumentException если партнер не найден
     */
    public Partner activateById(Long id) {
        Partner projectPartner = projectPartnerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Партнер с ID " + id + " не найден"));
        return activate(projectPartner);
    }

    /**
     * Деактивирует партнера проекта по ID.
     *
     * @param id ID партнера
     * @return деактивированный партнер проекта
     * @throws IllegalArgumentException если партнер не найден
     */
    public Partner deactivateById(Long id) {
        Partner projectPartner = projectPartnerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Партнер с ID " + id + " не найден"));
        return deactivate(projectPartner);
    }

    /**
     * Проверяет имеет ли партнер логотип.
     *
     * @param projectPartner партнер для проверки
     * @return true если у партнера есть логотип, иначе false
     */
    public boolean hasLogo(Partner projectPartner) {
        return projectPartner.hasLogo();
    }

    /**
     * Проверяет имеет ли партнер сайт.
     *
     * @param projectPartner партнер для проверки
     * @return true если у партнера есть сайт, иначе false
     */
    public boolean hasWebsite(Partner projectPartner) {
        return projectPartner.hasWebsite();
    }

    /**
     * Проверяет имеет ли партнер описание.
     *
     * @param projectPartner партнер для проверки
     * @return true если у партнера есть описание, иначе false
     */
    public boolean hasDescription(Partner projectPartner) {
        return projectPartner.hasDescription();
    }

    /**
     * Проверяет имеет ли партнер контактную информацию.
     *
     * @param projectPartner партнер для проверки
     * @return true если у партнера есть контактная информация, иначе false
     */
    public boolean hasContactInfo(Partner projectPartner) {
        return projectPartner.hasContactInfo();
    }

    /**
     * Получает полную контактную информацию партнера в виде строки.
     *
     * @param projectPartner партнер
     * @return форматированная контактная информация
     */
    public String getFormattedContactInfo(Partner projectPartner) {
        return projectPartner.getFormattedContactInfo();
    }

    /**
     * Получает URL сайта с протоколом.
     *
     * @param projectPartner партнер
     * @return полный URL с протоколом
     */
    public String getFullWebsiteUrl(Partner projectPartner) {
        return projectPartner.getFullWebsiteUrl();
    }

    /**
     * Получает отображаемое имя типа партнерства на русском.
     *
     * @param projectPartner партнер
     * @return русское название типа партнерства
     */
    public String getPartnerTypeDisplayNameRu(Partner projectPartner) {
        return projectPartner.getPartnerTypeDisplayNameRu();
    }

    /**
     * Получает отображаемое имя типа партнерства на английском.
     *
     * @param projectPartner партнер
     * @return английское название типа партнерства
     */
    public String getPartnerTypeDisplayNameEn(Partner projectPartner) {
        return projectPartner.getPartnerTypeDisplayNameEn();
    }

    /**
     * Проверяет является ли партнер спонсором.
     *
     * @param projectPartner партнер для проверки
     * @return true если партнер является спонсором, иначе false
     */
    public boolean isSponsor(Partner projectPartner) {
        return projectPartner.isSponsor();
    }

    /**
     * Проверяет является ли партнер информационным партнером.
     *
     * @param projectPartner партнер для проверки
     * @return true если партнер является информационным партнером, иначе false
     */
    public boolean isInformationPartner(Partner projectPartner) {
        return projectPartner.isInformationPartner();
    }

    /**
     * Получает инициалы партнера (для заглушки логотипа).
     *
     * @param projectPartner партнер
     * @return инициалы партнера (2 буквы)
     */
    public String getInitials(Partner projectPartner) {
        return projectPartner.getInitials();
    }

    /**
     * Обновляет порядок сортировки партнеров проекта.
     *
     * @param projectPartners список партнеров с обновленными sortOrder
     * @return список обновленных партнеров
     */
    public List<Partner> updateSortOrder(List<Partner> projectPartners) {
        return projectPartnerRepository.saveAll(projectPartners);
    }
}
