package com.healthcare.config;

import com.healthcare.entity.Doctor;
import com.healthcare.entity.Patient;
import com.healthcare.entity.Visit;
import com.healthcare.repository.DoctorRepository;
import com.healthcare.repository.PatientRepository;
import com.healthcare.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@ConditionalOnProperty(name = "app.data.initialize", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private VisitRepository visitRepository;

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (doctorRepository.count() > 0) {
            return;
        }

        // Create doctors
        List<Doctor> doctors = createDoctors();
        doctorRepository.saveAll(doctors);

        // Create patients
        List<Patient> patients = createPatients();
        patientRepository.saveAll(patients);

        // Create visits
        createVisits(doctors, patients);
    }

    private List<Doctor> createDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        
        // US doctors with different timezones
        doctors.add(new Doctor("John", "Smith", "America/New_York"));
        doctors.add(new Doctor("Emily", "Johnson", "America/Los_Angeles"));
        doctors.add(new Doctor("Michael", "Brown", "America/Chicago"));
        doctors.add(new Doctor("Sarah", "Davis", "America/Denver"));
        doctors.add(new Doctor("David", "Wilson", "America/New_York"));
        doctors.add(new Doctor("Lisa", "Anderson", "America/Los_Angeles"));
        doctors.add(new Doctor("Robert", "Taylor", "America/Chicago"));
        doctors.add(new Doctor("Jennifer", "Thomas", "America/Denver"));
        doctors.add(new Doctor("William", "Jackson", "America/New_York"));
        doctors.add(new Doctor("Maria", "White", "America/Los_Angeles"));

        return doctors;
    }

    private List<Patient> createPatients() {
        List<Patient> patients = new ArrayList<>();
        
        String[] firstNames = {"James", "Mary", "John", "Patricia", "Robert", "Jennifer", "Michael", "Linda", 
                              "William", "Elizabeth", "David", "Barbara", "Richard", "Susan", "Joseph", "Jessica",
                              "Thomas", "Sarah", "Christopher", "Karen", "Charles", "Nancy", "Daniel", "Lisa",
                              "Matthew", "Betty", "Anthony", "Helen", "Mark", "Sandra", "Donald", "Donna",
                              "Steven", "Carol", "Paul", "Ruth", "Andrew", "Sharon", "Joshua", "Michelle",
                              "Kenneth", "Laura", "Kevin", "Sarah", "Brian", "Kimberly", "George", "Deborah",
                              "Timothy", "Dorothy", "Ronald", "Lisa", "Jason", "Nancy", "Edward", "Karen",
                              "Jeffrey", "Betty", "Ryan", "Helen", "Jacob", "Sandra", "Gary", "Donna",
                              "Nicholas", "Carol", "Eric", "Ruth", "Jonathan", "Sharon", "Stephen", "Michelle",
                              "Larry", "Laura", "Justin", "Sarah", "Scott", "Kimberly", "Brandon", "Deborah",
                              "Benjamin", "Dorothy", "Samuel", "Amy", "Gregory", "Angela", "Alexander", "Ashley",
                              "Patrick", "Brenda", "Jack", "Emma", "Dennis", "Olivia", "Jerry", "Cynthia",
                              "Tyler", "Marie", "Aaron", "Janet", "Jose", "Catherine", "Henry", "Frances",
                              "Douglas", "Christine", "Adam", "Samantha", "Nathan", "Debra", "Peter", "Rachel",
                              "Zachary", "Carolyn", "Kyle", "Janet", "Noah", "Virginia", "Alan", "Maria",
                              "Ethan", "Heather", "Jeremy", "Diane", "Mason", "Julie", "Christian", "Joyce",
                              "Keith", "Victoria", "Roger", "Kelly", "Terry", "Christina", "Sean", "Joan",
                              "Gerald", "Evelyn", "Harold", "Judith", "Carl", "Andrea", "Arthur", "Hannah",
                              "Ryan", "Jacqueline", "Lawrence", "Martha", "Wayne", "Gloria", "Roy", "Teresa",
                              "Louis", "Sara", "Philip", "Janice", "Bobby", "Julia", "Johnny", "Marie",
                              "Eugene", "Madison", "Howard", "Grace", "Arthur", "Judy", "Albert", "Theresa",
                              "Ralph", "Beverly", "Joe", "Denise", "Willie", "Marilyn", "Elijah", "Amber",
                              "Wayne", "Danielle", "Eugene", "Brittany", "Ralph", "Diana", "Mason", "Abigail",
                              "Roy", "Jane", "Eugene", "Lori", "Ralph", "Tammy", "Eugene", "Jean"};

        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
                             "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
                             "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
                             "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker",
                             "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
                             "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell",
                             "Carter", "Roberts", "Gomez", "Phillips", "Evans", "Turner", "Diaz", "Parker",
                             "Cruz", "Edwards", "Collins", "Reyes", "Stewart", "Morris", "Morales", "Murphy",
                             "Cook", "Rogers", "Gutierrez", "Ortiz", "Morgan", "Cooper", "Peterson", "Bailey",
                             "Reed", "Kelly", "Howard", "Ramos", "Kim", "Cox", "Ward", "Richardson", "Watson",
                             "Brooks", "Chavez", "Wood", "James", "Bennett", "Gray", "Mendoza", "Ruiz", "Hughes",
                             "Price", "Alvarez", "Castillo", "Sanders", "Patel", "Myers", "Long", "Ross", "Foster",
                             "Jimenez", "Powell", "Jenkins", "Perry", "Russell", "Sullivan", "Bell", "Coleman",
                             "Butler", "Henderson", "Barnes", "Gonzales", "Fisher", "Vasquez", "Simmons", "Romero",
                             "Jordan", "Patterson", "Alexander", "Hamilton", "Graham", "Reynolds", "Griffin",
                             "Wallace", "Moreno", "West", "Cole", "Hayes", "Bryant", "Herrera", "Gibson",
                             "Ellis", "Tran", "Medina", "Aguilar", "Stevens", "Murray", "Ford", "Castro",
                             "Marshall", "Owens", "Harrison", "Fernandez", "McDonald", "Woods", "Washington",
                             "Kennedy", "Wells", "Vargas", "Henry", "Chen", "Freeman", "Webb", "Tucker",
                             "Guzman", "Burns", "Crawford", "Olson", "Simpson", "Porter", "Hunter", "Gordon",
                             "Mendez", "Silva", "Shaw", "Snyder", "Mason", "Dixon", "Munoz", "Hunt", "Hicks",
                             "Holmes", "Palmer", "Wagner", "Black", "Robertson", "Boyd", "Rose", "Stone",
                             "Salazar", "Fox", "Warren", "Mills", "Meyer", "Rice", "Schmidt", "Garza", "Daniels",
                             "Ferguson", "Nichols", "Stephens", "Soto", "Weaver", "Ryan", "Gardner", "Payne",
                             "Grant", "Dunn", "Kelley", "Spencer", "Hawkins", "Arnold", "Pierce", "Vazquez",
                             "Hansen", "Peters", "Santos", "Hart", "Bradley", "Knight", "Elliott", "Cunningham",
                             "Duncan", "Armstrong", "Hudson", "Carroll", "Lane", "Riley", "Andrews", "Alvarado",
                             "Ray", "Delgado", "Berry", "Perkins", "Hoffman", "Johnston", "Matthews", "Pena",
                             "Richards", "Contreras", "Willis", "Carpenter", "Lawrence", "Sandoval", "Guerrero",
                             "George", "Chapman", "Rios", "Estrada", "Ortega", "Watkins", "Greene", "Nunez",
                             "Wheeler", "Valdez", "Harper", "Burton", "Lynch", "Santana", "Austin", "Carr",
                             "Maldonado", "Terry", "Jimenez", "Carrillo", "Macias", "Krueger", "Robbins", "Hess",
                             "Reed", "Acosta", "Hines", "Benson", "Silva", "Garrett", "Walsh", "Daniels",
                             "Norman", "Hogan", "Morton", "Stokes", "Mack", "Pace", "Farrell", "Gaines",
                             "Blair", "Dominguez", "Bond", "Brock", "Cain", "Briggs", "Bryan", "Cannon",
                             "Casey", "Castro", "Cross", "Curry", "Erickson", "Farmer", "Fletcher", "Garcia",
                             "Gibbs", "Gill", "Glover", "Goodman", "Hampton", "Harvey", "Higgins", "Horton",
                             "Howell", "Ingram", "Jefferson", "Jennings", "Jensen", "Joseph", "Keith", "Lambert",
                             "Larson", "Lowe", "Lucas", "Mack", "Maldonado", "Marshall", "Martin", "Maxwell",
                             "McBride", "McDonald", "McKinney", "Mendoza", "Meyer", "Miller", "Molina", "Montgomery",
                             "Morales", "Morrison", "Murphy", "Murray", "Nelson", "Newman", "Nguyen", "Nichols",
                             "Norman", "Norris", "Norton", "Nunez", "O'Brien", "O'Connor", "Odom", "Oliver",
                             "Olsen", "Ortega", "Ortiz", "Owens", "Pacheco", "Padilla", "Page", "Palmer",
                             "Parker", "Parks", "Parrish", "Parsons", "Patel", "Patrick", "Patterson", "Patton",
                             "Paul", "Payne", "Pearson", "Peck", "Pena", "Perez", "Perkins", "Perry", "Peters",
                             "Peterson", "Phelps", "Phillips", "Pierce", "Pittman", "Pitts", "Porter", "Potter",
                             "Powell", "Powers", "Pratt", "Preston", "Price", "Prince", "Pruitt", "Pugh",
                             "Quinn", "Ramirez", "Ramos", "Ramsey", "Randall", "Randolph", "Rasmussen", "Ray",
                             "Raymond", "Reed", "Reese", "Reeves", "Reid", "Reyes", "Reynolds", "Rhodes",
                             "Rice", "Rich", "Richards", "Richardson", "Riley", "Rios", "Rivas", "Rivera",
                             "Robbins", "Roberts", "Robertson", "Robinson", "Robles", "Rodriguez", "Rogers",
                             "Rojas", "Roman", "Romero", "Roach", "Ross", "Roth", "Rowe", "Rowland", "Roy",
                             "Rubio", "Rush", "Russell", "Russo", "Ryan", "Salas", "Salazar", "Salinas",
                             "Sampson", "Sanchez", "Sanders", "Sandoval", "Santana", "Santiago", "Santos",
                             "Saunders", "Sawyer", "Schmidt", "Schneider", "Schroeder", "Schultz", "Schwartz",
                             "Scott", "Sellers", "Serrano", "Sexton", "Shaffer", "Shannon", "Sharp", "Shaw",
                             "Shelton", "Sherman", "Shields", "Short", "Silva", "Simmons", "Simon", "Simpson",
                             "Sims", "Singleton", "Skinner", "Slater", "Smith", "Snider", "Snow", "Snyder",
                             "Solis", "Solomon", "Sosa", "Soto", "Sparks", "Spencer", "Stafford", "Stanley",
                             "Stanton", "Stark", "Steele", "Stephens", "Stephenson", "Stevens", "Stevenson",
                             "Stewart", "Stokes", "Stone", "Stout", "Strickland", "Strong", "Stuart", "Suarez",
                             "Sullivan", "Summers", "Sutton", "Swanson", "Sweeney", "Sweet", "Sykes", "Talley",
                             "Tanner", "Tate", "Taylor", "Terrell", "Terry", "Thomas", "Thompson", "Thornton",
                             "Tillman", "Todd", "Torres", "Townsend", "Tran", "Travis", "Trevino", "Trujillo",
                             "Tucker", "Turner", "Tyler", "Tyson", "Underwood", "Valdez", "Valencia", "Valentine",
                             "Valenzuela", "Vance", "Vang", "Vargas", "Vasquez", "Vaughan", "Vaughn", "Vazquez",
                             "Vega", "Velasquez", "Velazquez", "Velez", "Villa", "Villanueva", "Villarreal",
                             "Villegas", "Vincent", "Vinson", "Wade", "Wagner", "Walker", "Wall", "Wallace",
                             "Waller", "Walls", "Walsh", "Walter", "Walters", "Walton", "Ward", "Ware", "Warner",
                             "Warren", "Washington", "Waters", "Watkins", "Watson", "Watts", "Weaver", "Webb",
                             "Weber", "Webster", "Weeks", "Weiss", "Welch", "Wells", "West", "Wheeler", "Whitaker",
                             "White", "Whitehead", "Whitfield", "Whitley", "Whitney", "Wiggins", "Wilcox", "Wilder",
                             "Wiley", "Wilkerson", "Wilkins", "Wilkinson", "William", "Williams", "Williamson",
                             "Willis", "Wilson", "Winters", "Wise", "Witt", "Wolf", "Wolfe", "Wong", "Wood",
                             "Woodard", "Woods", "Woodward", "Wooten", "Workman", "Wright", "Wyatt", "Wynn",
                             "Yang", "Yates", "York", "Young", "Zamora", "Zavala", "Zimmerman", "Zuniga"};

        for (int i = 0; i < 1000; i++) {
            String firstName = firstNames[random.nextInt(firstNames.length)];
            String lastName = lastNames[random.nextInt(lastNames.length)];
            patients.add(new Patient(firstName, lastName));
        }

        return patients;
    }

    private void createVisits(List<Doctor> doctors, List<Patient> patients) {
        List<Visit> visits = new ArrayList<>();
        
        // Create visits for the past 2 years
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoYearsAgo = now.minusYears(2);

        for (int i = 0; i < 5000; i++) {
            Doctor doctor = doctors.get(random.nextInt(doctors.size()));
            Patient patient = patients.get(random.nextInt(patients.size()));
            
            // Generate random date within the last 2 years
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(twoYearsAgo, now);
            LocalDateTime randomDate = twoYearsAgo.plusDays(random.nextInt((int) daysBetween));
            
            // Random hour between 8 AM and 6 PM
            int hour = 8 + random.nextInt(10);
            int minute = random.nextInt(4) * 15; // 0, 15, 30, 45
            
            LocalDateTime startTime = randomDate.withHour(hour).withMinute(minute);
            LocalDateTime endTime = startTime.plusHours(1); // 1-hour appointments
            
            // Convert to doctor's timezone for storage
            ZoneId doctorTimezone = ZoneId.of(doctor.getTimezone());
            LocalDateTime startInDoctorTz = startTime.atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(doctorTimezone).toLocalDateTime();
            LocalDateTime endInDoctorTz = endTime.atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(doctorTimezone).toLocalDateTime();
            
            visits.add(new Visit(startInDoctorTz, endInDoctorTz, patient, doctor));
        }
        
        visitRepository.saveAll(visits);
    }
}
