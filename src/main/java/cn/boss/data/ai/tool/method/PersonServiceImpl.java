package cn.boss.data.ai.tool.method;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the PersonService interface using an in-memory data store.
 */
@Service
@Slf4j
public class PersonServiceImpl implements PersonService {

    private final Map<Integer, Person> personStore = new ConcurrentHashMap<>();

    private AtomicInteger idGenerator;

    private static final String CSV_DATA = """
            Id,FirstName,LastName,Email,Sex,IpAddress,JobTitle,Age
            1,Fons,Tollfree,ftollfree0@senate.gov,Male,55.1 Tollfree Lane,Research Associate,31
            2,Emlynne,Tabourier,etabourier1@networksolutions.com,Female,18 Tabourier Way,Associate Professor,38
            3,Shae,Johncey,sjohncey2@yellowpages.com,Male,1 Johncey Circle,Structural Analysis Engineer,30
            4,Sebastien,Bradly,sbradly3@mapquest.com,Male,2 Bradly Hill,Chief Executive Officer,40
            5,Harriott,Kitteringham,hkitteringham4@typepad.com,Female,3 Kitteringham Drive,VP Sales,47
            6,Anallise,Parradine,aparradine5@miibeian.gov.cn,Female,4 Parradine Street,Analog Circuit Design manager,44
            7,Gorden,Kirkbright,gkirkbright6@reuters.com,Male,5 Kirkbright Plaza,Senior Editor,40
            8,Veradis,Ledwitch,vledwitch7@google.com.au,Female,6 Ledwitch Avenue,Computer Systems Analyst IV,44
            9,Agnesse,Penhalurick,apenhalurick8@google.it,Female,7 Penhalurick Terrace,Automation Specialist IV,41
            10,Bibby,Hutable,bhutable9@craigslist.org,Female,8 Hutable Place,Account Representative I,43
            """;

    @PostConstruct
    private void initializeData() {
        log.info("Initializing PersonService data store...");
        int maxId = loadDataFromCsv();
        idGenerator = new AtomicInteger(maxId);
        log.info("PersonService initialized with {} records. Next ID: {}", personStore.size(), idGenerator.get() + 1);
    }

    private int loadDataFromCsv() {
        final AtomicInteger currentMaxId = new AtomicInteger(0);
        personStore.clear();
        try (Stream<String> lines = CSV_DATA.lines().skip(1)) {
            lines.forEach(line -> {
                try {
                    String[] fields = line.split(",", 8);
                    if (fields.length == 8) {
                        int id = Integer.parseInt(fields[0].trim());
                        String firstName = fields[1].trim();
                        String lastName = fields[2].trim();
                        String email = fields[3].trim();
                        String sex = fields[4].trim();
                        String ipAddress = fields[5].trim();
                        String jobTitle = fields[6].trim();
                        int age = Integer.parseInt(fields[7].trim());

                        Person person = new Person(id, firstName, lastName, email, sex, ipAddress, jobTitle, age);
                        personStore.put(id, person);
                        currentMaxId.updateAndGet(max -> Math.max(max, id));
                    } else {
                        log.warn("Skipping malformed CSV line (expected 8 fields, found {}): {}", fields.length, line);
                    }
                } catch (NumberFormatException e) {
                    log.warn("Skipping line due to parsing error (ID or Age): {} - Error: {}", line, e.getMessage());
                } catch (Exception e) {
                    log.error("Skipping line due to unexpected error: {} - Error: {}", line, e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            log.error("Fatal error reading embedded CSV data: {}", e.getMessage(), e);
        }
        return currentMaxId.get();
    }

    @Override
    @Tool(name = "ps_create_person", description = "Create a new person record in the in-memory store.")
    public Person createPerson(Person personData) {
        if (personData == null) {
            throw new IllegalArgumentException("Person data cannot be null");
        }
        int newId = idGenerator.incrementAndGet();
        Person newPerson = new Person(newId, personData.firstName(), personData.lastName(),
                personData.email(), personData.sex(), personData.ipAddress(), personData.jobTitle(), personData.age());
        personStore.put(newId, newPerson);
        log.debug("Created person: {}", newPerson);
        return newPerson;
    }

    @Override
    @Tool(name = "ps_get_person_by_id", description = "Retrieve a person record by ID from the in-memory store.")
    public Optional<Person> getPersonById(int id) {
        Person person = personStore.get(id);
        log.debug("Retrieved person by ID {}: {}", id, person);
        return Optional.ofNullable(person);
    }

    @Override
    @Tool(name = "ps_get_all_persons", description = "Retrieve all person records from the in-memory store.")
    public List<Person> getAllPersons() {
        List<Person> allPersons = personStore.values().stream().toList();
        log.debug("Retrieved all persons (count: {})", allPersons.size());
        return allPersons;
    }

    @Override
    @Tool(name = "ps_update_person", description = "Update an existing person record by ID in the in-memory store.")
    public boolean updatePerson(int id, Person updatedPersonData) {
        if (updatedPersonData == null) {
            throw new IllegalArgumentException("Updated person data cannot be null");
        }
        Person result = personStore.computeIfPresent(id, (key, existingPerson) ->
                new Person(id, updatedPersonData.firstName(), updatedPersonData.lastName(),
                        updatedPersonData.email(), updatedPersonData.sex(), updatedPersonData.ipAddress(),
                        updatedPersonData.jobTitle(), updatedPersonData.age()));
        boolean updated = result != null;
        log.debug("Update attempt for ID {}: {}", id, updated ? "Successful" : "Failed (Not Found)");
        return updated;
    }

    @Override
    @Tool(name = "ps_delete_person", description = "Delete a person record by ID from the in-memory store.")
    public boolean deletePerson(int id) {
        boolean removed = personStore.remove(id) != null;
        log.debug("Delete attempt for ID {}: {}", id, removed ? "Successful" : "Failed (Not Found)");
        return removed;
    }

    @Override
    @Tool(name = "ps_search_by_job_title", description = "Search for persons by job title in the in-memory store.")
    public List<Person> searchByJobTitle(String jobTitleQuery) {
        if (jobTitleQuery == null || jobTitleQuery.isBlank()) {
            return Collections.emptyList();
        }
        String lowerCaseQuery = jobTitleQuery.toLowerCase();
        List<Person> results = personStore.values().stream()
                .filter(person -> person.jobTitle() != null && person.jobTitle().toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toList());
        return Collections.unmodifiableList(results);
    }

    @Override
    @Tool(name = "ps_filter_by_sex", description = "Filters Persons by sex (case-insensitive).")
    public List<Person> filterBySex(String sex) {
        if (sex == null || sex.isBlank()) {
            return Collections.emptyList();
        }
        List<Person> results = personStore.values().stream()
                .filter(person -> person.sex() != null && person.sex().equalsIgnoreCase(sex))
                .collect(Collectors.toList());
        return Collections.unmodifiableList(results);
    }

    @Override
    @Tool(name = "ps_filter_by_age", description = "Filters Persons by age.")
    public List<Person> filterByAge(int age) {
        if (age < 0) {
            return Collections.emptyList();
        }
        List<Person> results = personStore.values().stream()
                .filter(person -> person.age() == age)
                .collect(Collectors.toList());
        return Collections.unmodifiableList(results);
    }

}
