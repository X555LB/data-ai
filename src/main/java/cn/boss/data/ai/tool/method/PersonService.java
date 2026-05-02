package cn.boss.data.ai.tool.method;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing Person data.
 */
public interface PersonService {

    Person createPerson(Person personData);

    Optional<Person> getPersonById(int id);

    List<Person> getAllPersons();

    boolean updatePerson(int id, Person updatedPersonData);

    boolean deletePerson(int id);

    List<Person> searchByJobTitle(String jobTitleQuery);

    List<Person> filterBySex(String sex);

    List<Person> filterByAge(int age);

}
