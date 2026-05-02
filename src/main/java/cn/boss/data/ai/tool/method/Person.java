package cn.boss.data.ai.tool.method;

/**
 * Represents a person with basic information.
 */
public record Person(
        int id,
        String firstName,
        String lastName,
        String email,
        String sex,
        String ipAddress,
        String jobTitle,
        int age
) {
}
