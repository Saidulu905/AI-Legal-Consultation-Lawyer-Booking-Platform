package com.legal.platform.config;

import com.legal.platform.model.*;
import com.legal.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LawyerProfileRepository lawyerProfileRepository;
    private final AvailabilityRepository availabilityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Categories if empty
        if (categoryRepository.count() == 0) {
            List<Category> categories = Arrays.asList(
                Category.builder().name("Labor Law").description("Employment disputes, wage claims, harassment, and wrongful termination").build(),
                Category.builder().name("Criminal Law").description("Defense representation for felonies, misdemeanors, and traffic violations").build(),
                Category.builder().name("Family Law").description("Divorce, child custody, alimony, and prenuptial agreements").build(),
                Category.builder().name("Corporate Law").description("Business formation, contracts, mergers, and compliance").build(),
                Category.builder().name("Intellectual Property").description("Patents, trademarks, copyrights, and trade secrets").build()
            );
            categoryRepository.saveAll(categories);
            System.out.println("Seeded 5 legal categories.");
        }

        // 2. Seed Admin User if empty
        if (userRepository.findByEmail("admin@legalplatform.com").isEmpty()) {
            User admin = User.builder()
                .name("Platform Admin")
                .email("admin@legalplatform.com")
                .password(passwordEncoder.encode("AdminPassword123"))
                .role(Role.ADMIN)
                .isVerified(true)
                .build();
            userRepository.save(admin);
            System.out.println("Seeded default admin user: admin@legalplatform.com");
        }

        // 3. Seed Client User if empty
        if (userRepository.findByEmail("client@legalplatform.com").isEmpty()) {
            User client = User.builder()
                .name("John Doe")
                .email("client@legalplatform.com")
                .password(passwordEncoder.encode("ClientPassword123"))
                .role(Role.CLIENT)
                .isVerified(true)
                .build();
            userRepository.save(client);
            System.out.println("Seeded default client user: client@legalplatform.com");
        }

        // 4. Seed Lawyer User if empty
        if (userRepository.findByEmail("lawyer@legalplatform.com").isEmpty()) {
            User lawyerUser = User.builder()
                .name("Sarah Jenkins")
                .email("lawyer@legalplatform.com")
                .password(passwordEncoder.encode("LawyerPassword123"))
                .role(Role.LAWYER)
                .isVerified(true)
                .build();
            User savedLawyerUser = userRepository.save(lawyerUser);

            Category laborLaw = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equals("Labor Law"))
                .findFirst().orElse(null);

            LawyerProfile lawyerProfile = LawyerProfile.builder()
                .user(savedLawyerUser)
                .specialization("Labor & Employment Law specialist")
                .bio("Experienced attorney with over 10 years representing clients in wage claims, severance negotiations, and workplace disputes.")
                .experienceYears(10)
                .hourlyRate(150.0)
                .category(laborLaw)
                .isApproved(true) // Automatically approve seeded lawyer
                .build();
            LawyerProfile savedProfile = lawyerProfileRepository.save(lawyerProfile);
            System.out.println("Seeded default lawyer profile for: lawyer@legalplatform.com");

            // Seed availability slots
            List<Availability> slots = Arrays.asList(
                Availability.builder().lawyer(savedProfile).dayOfWeek("Monday").startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(10, 0)).isAvailable(true).build(),
                Availability.builder().lawyer(savedProfile).dayOfWeek("Monday").startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0)).isAvailable(true).build(),
                Availability.builder().lawyer(savedProfile).dayOfWeek("Wednesday").startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(15, 0)).isAvailable(true).build(),
                Availability.builder().lawyer(savedProfile).dayOfWeek("Wednesday").startTime(LocalTime.of(15, 0)).endTime(LocalTime.of(16, 0)).isAvailable(true).build(),
                Availability.builder().lawyer(savedProfile).dayOfWeek("Friday").startTime(LocalTime.of(11, 0)).endTime(LocalTime.of(12, 0)).isAvailable(true).build()
            );
            availabilityRepository.saveAll(slots);
            System.out.println("Seeded default availability slots for Sarah Jenkins.");
        }
    }
}
