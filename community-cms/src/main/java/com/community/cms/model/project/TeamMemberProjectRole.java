package com.community.cms.model.project;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "team_member_project_roles",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_team_member_project",
                columnNames = {"team_member_id", "project_id"}
        )
)
public class TeamMemberProjectRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "team_member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_tmpr_team_member")
    )
    private TeamMember teamMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "project_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_tmpr_project")
    )
    private Project project;

    @Column(name = "role", nullable = false, length = 100)
    private String role;

    @Column(name = "joined_date")
    private LocalDate joinedDate;

    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Конструкторы
    public TeamMemberProjectRole() {
        this.createdAt = LocalDateTime.now();
    }

    public TeamMemberProjectRole(TeamMember teamMember, Project project, String role) {
        this();
        this.teamMember = teamMember;
        this.project = project;
        this.role = role;
        this.joinedDate = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TeamMember getTeamMember() {
        return teamMember;
    }

    public void setTeamMember(TeamMember teamMember) {
        this.teamMember = teamMember;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDate getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDate joinedDate) {
        this.joinedDate = joinedDate;
    }

    public String getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(String responsibilities) {
        this.responsibilities = responsibilities;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // equals() и hashCode() для корректной работы коллекций
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeamMemberProjectRole that = (TeamMemberProjectRole) o;

        if (id != null && that.id != null) {
            return id.equals(that.id);
        }

        return teamMember != null && teamMember.equals(that.teamMember) &&
                project != null && project.equals(that.project);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "TeamMemberProjectRole{" +
                "id=" + id +
                ", teamMember=" + (teamMember != null ? teamMember.getFullName() : "null") +
                ", project=" + (project != null ? project.getTitle() : "null") +
                ", role='" + role + '\'' +
                ", joinedDate=" + joinedDate +
                '}';
    }

    // Дополнительные методы
    public boolean isRole(String roleName) {
        return role != null && role.equalsIgnoreCase(roleName);
    }

    public boolean hasResponsibilities() {
        return responsibilities != null && !responsibilities.trim().isEmpty();
    }

    public String getDisplayInfo() {
        return String.format("%s - %s в проекте '%s'",
                teamMember != null ? teamMember.getFullName() : "Неизвестный",
                role,
                project != null ? project.getTitle() : "Неизвестный проект"
        );
    }
}
