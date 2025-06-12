// package com.codefathers.service;

// import com.codefathers.model.dto.CreateEmployeeDTO;
// import com.codefathers.model.entity.Employee;
// import com.codefathers.model.enums.EmployeeGender;
// import com.codefathers.model.enums.EmployeeRole;
// import com.codefathers.repository.interfaces.EmployeeRepository;
// import jakarta.validation.ConstraintViolation;
// import jakarta.validation.Validator;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.*;

// import org.mockito.junit.jupiter.MockitoExtension;

// import java.time.LocalDate;
// import java.util.*;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class EmployeeServiceTest {

//     @Mock
//     private EmployeeRepository employeeRepository;

//     @Mock
//     private Validator validator;

//     @InjectMocks
//     private EmployeeService employeeService;

//     private CreateEmployeeDTO validDTO;
//     private Employee validEmployee;

//     @BeforeEach
//     void setup() {
//         validDTO = new CreateEmployeeDTO(
//                 "João da Silva",
//                 LocalDate.of(1990, 1, 1),
//                 EmployeeGender.MALE,
//                 EmployeeRole.SAC
//         );

//         validEmployee = Employee.builder()
//                 .id(UUID.randomUUID())
//                 .fullName("João da Silva")
//                 .role(EmployeeRole.SAC)
//                 .gender(EmployeeGender.MALE)
//                 .birthDate(LocalDate.of(1990, 1, 1))
//                 .build();
//     }

//     @Test
//     void testCreateEmployee_withValidData_savesSuccessfully() {
//         lenient().when(validator.validate(validDTO)).thenReturn(Collections.emptySet());

//         assertDoesNotThrow(() -> employeeService.createEmployee(validDTO));

//         verify(employeeRepository).saveEmployee(any(Employee.class));
//     }

//     @Test
//     void testCreateEmployee_withInvalidDTO_throwsException() {
//         ConstraintViolation<CreateEmployeeDTO> violation = mock(ConstraintViolation.class);
//         when(violation.getMessage()).thenReturn("Nome é obrigatório");

//         Set<ConstraintViolation<CreateEmployeeDTO>> violations = Set.of(violation);
//         when(validator.validate(validDTO)).thenReturn(violations);

//         IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
//                 () -> employeeService.createEmployee(validDTO));

//         assertTrue(exception.getMessage().contains("Erros de validação"));
//         verify(employeeRepository, never()).saveEmployee(any());
//     }

//     @Test
//     void testCreateEmployee_withAgeBelow16_throwsException() {
//         validDTO = new CreateEmployeeDTO(
//                 "João da Silva",
//                 LocalDate.now().minusYears(15),
//                 EmployeeGender.MALE,
//                 EmployeeRole.SAC
//         );

//         lenient().when(validator.validate(validDTO)).thenReturn(Collections.emptySet());

//         IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
//                 () -> employeeService.createEmployee(validDTO));

//         assertEquals("O funcionário deve ter no mínimo 16 (dezesseis) anos para ser registrado", exception.getMessage());
//     }

//     @Test
//     void testEmployeeList_returnsEmployees() {
//         List<Employee> expectedList = List.of(validEmployee);
//         when(employeeRepository.listAllEmployees()).thenReturn(expectedList);

//         List<Employee> result = employeeService.employeeList();

//         assertEquals(1, result.size());
//         assertEquals("João da Silva", result.get(0).getFullName());
//     }

//     @Test
//     void testDeleteEmployeeByID_callsRepository() {
//         UUID id = UUID.randomUUID();

//         employeeService.deleteEmployeeByID(id);

//         verify(employeeRepository).deleteEmployeeByID(id);
//     }

//     @Test
//     void testFindEmployeeById_whenEmployeeExists_shouldPrint() {
//         UUID id = validEmployee.getId();
//         List<Employee> list = List.of(validEmployee);

//         when(employeeRepository.listAllEmployees()).thenReturn(list);

//         // Apenas garantindo que não lança exceção
//         assertDoesNotThrow(() -> employeeService.findEmployeeById(id));
//     }

//     @Test
//     void testFindEmployeeById_whenNotFound_shouldPrintNotFound() {
//         UUID id = UUID.randomUUID();
//         List<Employee> list = List.of(validEmployee);

//         when(employeeRepository.listAllEmployees()).thenReturn(list);

//         assertDoesNotThrow(() -> employeeService.findEmployeeById(id));
//     }

//     @Test
//     void testFindEmployeeById_whenExceptionThrown_shouldHandleSilently() {
//         UUID id = UUID.randomUUID();
//         when(employeeRepository.listAllEmployees()).thenThrow(new RuntimeException("Erro interno"));

//         assertDoesNotThrow(() -> employeeService.findEmployeeById(id));
//     }
// }
