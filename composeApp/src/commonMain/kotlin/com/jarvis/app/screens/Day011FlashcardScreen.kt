package com.jarvis.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jarvis.app.models.Flashcard

private val cbad2103Cards = listOf(
    Flashcard("1", "What is an information system?", "An organised combination of hardware, software, data, processes, and people that collects, processes, stores, and distributes information.", "1 · Information Systems"),
    Flashcard("2", "How are data and information different?", "Data are raw facts and figures. Information is data processed into a useful form.", "1 · Information Systems"),
    Flashcard("3", "What are the five information-system components?", "Hardware, software, data, processes, and people.", "1 · Information Systems"),
    Flashcard("4", "What does a transaction processing system (TPS) support?", "Routine day-to-day operational transactions such as sales, payroll, and inventory updates.", "1 · Information Systems"),
    Flashcard("5", "What does a management information system (MIS) provide?", "Periodic, structured reports that help middle managers monitor and control operations.", "1 · Information Systems"),
    Flashcard("6", "What does a decision support system (DSS) support?", "Semi-structured decisions through data, analytical models, and interactive what-if analysis.", "1 · Information Systems"),
    Flashcard("7", "What does an executive support system (ESS) support?", "Strategic, unstructured decisions by senior management using summarised internal and external information.", "1 · Information Systems"),
    Flashcard("8", "Which information qualities matter to users?", "Accuracy, relevance, completeness, timeliness, accessibility, and appropriate level of detail.", "1 · Information Systems"),
    Flashcard("9", "What six skill areas should a systems analyst have?", "Technical, business, analytical, interpersonal, managerial, and ethical skills.", "1 · Information Systems"),
    Flashcard("10", "What roles can a systems analyst perform?", "Consultant, supporting expert, and agent of change.", "1 · Information Systems"),

    Flashcard("11", "What are the five primary SDLC phases?", "Planning, analysis, design, implementation, and maintenance.", "2 · Methodology"),
    Flashcard("12", "What happens during system planning?", "Business need is identified, project feasibility assessed, scope set, and project plan prepared.", "2 · Methodology"),
    Flashcard("13", "What happens during system analysis?", "Current system and business needs are studied; requirements are gathered, structured, and validated.", "2 · Methodology"),
    Flashcard("14", "What happens during system design?", "Logical requirements are converted into physical architecture, database, interface, program, and security specifications.", "2 · Methodology"),
    Flashcard("15", "What happens during implementation?", "System is built or acquired, tested, documented, installed, and users are trained.", "2 · Methodology"),
    Flashcard("16", "What happens during maintenance?", "System is corrected, adapted, improved, protected, and monitored after deployment.", "2 · Methodology"),
    Flashcard("17", "What is the heart of systems development?", "Repeated analysis, design, coding, and testing activities.", "2 · Methodology"),
    Flashcard("18", "Main weakness of traditional waterfall SDLC?", "It assumes stable requirements and makes late changes slow and costly because phases are sequential.", "2 · Methodology"),
    Flashcard("19", "What distinguishes Agile development?", "Short iterations, working software, close customer collaboration, feedback, and response to change.", "2 · Methodology"),
    Flashcard("20", "What is Scrum?", "A lightweight Agile process framework that organises work into time-boxed Sprints.", "2 · Methodology"),
    Flashcard("21", "What are core Scrum roles?", "Product Owner, Scrum Master, and Developers.", "2 · Methodology"),
    Flashcard("22", "What are key Scrum artefacts?", "Product Backlog, Sprint Backlog, and Increment.", "2 · Methodology"),
    Flashcard("23", "What is object-oriented analysis and design (OOAD)?", "An approach that models a system as interacting objects that combine data and behaviour.", "2 · Methodology"),

    Flashcard("24", "What are the two major system-planning activities?", "Project identification and selection; project initiation and planning.", "3 · Planning"),
    Flashcard("25", "Where can development projects originate?", "Top management, steering committees, user departments, development groups, or external forces.", "3 · Planning"),
    Flashcard("26", "How are candidate projects commonly ranked?", "Strategic alignment, potential benefits, resource availability, project size or duration, technical difficulty, and risk.", "3 · Planning"),
    Flashcard("27", "What is feasibility analysis?", "Assessment of whether a proposed project is practical and worth undertaking.", "3 · Planning"),
    Flashcard("28", "What is economic feasibility?", "Whether expected benefits justify development and operating costs.", "3 · Planning"),
    Flashcard("29", "What is technical feasibility?", "Whether required technology, infrastructure, and expertise can support the solution.", "3 · Planning"),
    Flashcard("30", "What is operational feasibility?", "Whether solution fits organisational practices and will be accepted and used.", "3 · Planning"),
    Flashcard("31", "What is schedule feasibility?", "Whether project can be completed within required time constraints.", "3 · Planning"),
    Flashcard("32", "What is legal or contractual feasibility?", "Whether project complies with laws, regulations, licences, and contractual obligations.", "3 · Planning"),
    Flashcard("33", "What is a baseline project plan (BPP)?", "Main planning document containing scope, benefits, costs, risks, schedule, resources, and recommended action.", "3 · Planning"),
    Flashcard("34", "What is a project scope statement (PSS)?", "A concise agreement defining project boundaries, objectives, deliverables, constraints, assumptions, and stakeholders.", "3 · Planning"),
    Flashcard("35", "What outcomes follow project initiation and planning?", "Accept project, reject it, or redirect it for revision or further study.", "3 · Planning"),

    Flashcard("36", "What is a requirement?", "A capability, feature, quality, or constraint a system must satisfy.", "4 · Requirements"),
    Flashcard("37", "Functional vs non-functional requirement?", "Functional states what system must do. Non-functional states quality or constraint, such as performance, security, or usability.", "4 · Requirements"),
    Flashcard("38", "Why analyse current procedures and documents?", "To discover existing workflows, business rules, data, problems, exceptions, and terminology.", "4 · Requirements"),
    Flashcard("39", "Main advantage of interviews?", "They provide rich detail and allow immediate probing and clarification.", "4 · Requirements"),
    Flashcard("40", "Main limitation of interviews?", "They consume time and may contain bias, incomplete recall, or inconsistent answers.", "4 · Requirements"),
    Flashcard("41", "Open-ended vs closed-ended interview questions?", "Open-ended questions invite explanation. Closed-ended questions request limited, specific answers.", "4 · Requirements"),
    Flashcard("42", "When is observation useful?", "When actual work may differ from documented or reported procedures.", "4 · Requirements"),
    Flashcard("43", "What is Joint Application Design (JAD)?", "A facilitated workshop where users, managers, and analysts jointly define and agree requirements.", "4 · Requirements"),
    Flashcard("44", "What is prototyping?", "Building a quick working model to clarify, test, and refine uncertain requirements through feedback.", "4 · Requirements"),
    Flashcard("45", "What is Business Process Re-engineering (BPR)?", "Fundamental redesign of business processes to achieve major improvements in cost, quality, service, or speed.", "4 · Requirements"),
    Flashcard("46", "Why is continual user involvement important?", "It improves requirement accuracy, ownership, acceptance, feedback speed, and change management.", "4 · Requirements"),

    Flashcard("47", "What does a data flow diagram (DFD) show?", "How data moves among external entities, processes, and data stores within a system.", "5 · Process Modelling"),
    Flashcard("48", "What are the four basic DFD symbols?", "Process, data flow, data store, and external entity/source/sink.", "5 · Process Modelling"),
    Flashcard("49", "How should a DFD process be named?", "With a verb phrase describing transformation, such as Validate Order.", "5 · Process Modelling"),
    Flashcard("50", "How should data flows, stores, and external entities be named?", "With meaningful noun phrases.", "5 · Process Modelling"),
    Flashcard("51", "What is a context diagram?", "Highest-level DFD showing entire system as one process and its data exchanges with external entities.", "5 · Process Modelling"),
    Flashcard("52", "What is DFD decomposition?", "Breaking a high-level process into a more detailed lower-level DFD.", "5 · Process Modelling"),
    Flashcard("53", "What is balancing in DFDs?", "Parent process inputs and outputs must match combined inputs and outputs in its child diagram.", "5 · Process Modelling"),
    Flashcard("54", "Can data flow directly between two external entities in a DFD?", "No. Data represented inside system boundary must pass through a process.", "5 · Process Modelling"),
    Flashcard("55", "Can data flow directly between two data stores?", "No. A process must transform or route it.", "5 · Process Modelling"),
    Flashcard("56", "Can data flow directly between an external entity and data store?", "No. It must pass through a process.", "5 · Process Modelling"),
    Flashcard("57", "What is a black hole process?", "A process with input but no meaningful output.", "5 · Process Modelling"),
    Flashcard("58", "What is a miracle process?", "A process that produces output without sufficient input.", "5 · Process Modelling"),
    Flashcard("59", "What is a grey hole process?", "A process whose outputs are not logically supported by its inputs.", "5 · Process Modelling"),
    Flashcard("60", "Logical vs physical DFD?", "Logical DFD shows what business work occurs. Physical DFD shows how, where, and by whom it is performed.", "5 · Process Modelling"),
    Flashcard("61", "How are DFD components documented?", "Process descriptions, data-flow descriptions, data-element definitions, and data-store descriptions in a data dictionary.", "5 · Process Modelling"),

    Flashcard("62", "What are three main system acquisition strategies?", "Custom development, packaged software, and outsourcing.", "6 · System Design"),
    Flashcard("63", "Main benefit and drawback of custom development?", "Best fit and control; usually highest time, cost, and development risk.", "6 · System Design"),
    Flashcard("64", "Main benefit and drawback of packaged software?", "Faster and less risky; may fit poorly and require process changes or customisation.", "6 · System Design"),
    Flashcard("65", "Main benefit and drawback of outsourcing?", "Access to external expertise and capacity; reduced control plus vendor and contract dependency.", "6 · System Design"),
    Flashcard("66", "What factors influence acquisition strategy?", "Business need, in-house skills, project size, time, cost, risk, strategic importance, integration, and vendor availability.", "6 · System Design"),
    Flashcard("67", "What is a request for proposal (RFP)?", "Formal document describing requirements and asking vendors to propose a solution, schedule, and price.", "6 · System Design"),
    Flashcard("68", "What is an alternative matrix?", "Table that scores solution options against weighted evaluation criteria.", "6 · System Design"),
    Flashcard("69", "Why move from logical to physical models?", "To convert business requirements into implementable decisions about people, technology, files, controls, and procedures.", "6 · System Design"),

    Flashcard("70", "What does architecture design define?", "System hardware, software, network, data, interfaces, and distribution of processing.", "7 · Architecture"),
    Flashcard("71", "What is client-server architecture?", "Clients request services or resources; one or more servers process requests and manage shared resources.", "7 · Architecture"),
    Flashcard("72", "What is two-tier architecture?", "Client communicates directly with database or application server, often combining presentation and business logic on client.", "7 · Architecture"),
    Flashcard("73", "What is three-tier architecture?", "Presentation, business/application logic, and data management are separated into tiers.", "7 · Architecture"),
    Flashcard("74", "What is n-tier architecture?", "System responsibilities are split across multiple specialised, independently managed layers or services.", "7 · Architecture"),
    Flashcard("75", "What is server-based architecture?", "Most processing and data storage occur centrally; client mainly handles input and display.", "7 · Architecture"),
    Flashcard("76", "What concerns shape mobile architecture?", "Intermittent networks, bandwidth, battery, device limits, responsive UI, local storage, synchronisation, and security.", "7 · Architecture"),
    Flashcard("77", "What is virtualisation?", "Software abstraction that allows multiple isolated virtual machines or resources to share physical hardware.", "7 · Architecture"),
    Flashcard("78", "What is cloud computing?", "On-demand network access to scalable shared computing resources, commonly billed by usage.", "7 · Architecture"),
    Flashcard("79", "How should architecture options be compared?", "Cost, scalability, performance, reliability, security, maintainability, compatibility, skills, and organisational constraints.", "7 · Architecture"),

    Flashcard("80", "What does a test plan define?", "Test scope, objectives, cases, data, environment, responsibilities, schedule, expected results, and acceptance criteria.", "8 · Implementation"),
    Flashcard("81", "What is unit testing?", "Testing an individual program, class, function, or module in isolation.", "8 · Implementation"),
    Flashcard("82", "What is integration testing?", "Testing interfaces and interactions between combined modules or subsystems.", "8 · Implementation"),
    Flashcard("83", "What is system testing?", "Testing complete integrated system against functional and non-functional requirements.", "8 · Implementation"),
    Flashcard("84", "What is acceptance testing?", "Users or customers verify system meets business needs and is ready for operational use.", "8 · Implementation"),
    Flashcard("85", "Alpha vs beta testing?", "Alpha occurs in controlled developer environment. Beta occurs with selected users in real operating conditions.", "8 · Implementation"),
    Flashcard("86", "What are four documentation types?", "Program, system, operations, and user documentation.", "8 · Implementation"),
    Flashcard("87", "What does program documentation support?", "Understanding, debugging, modifying, and maintaining source code and program logic.", "8 · Implementation"),
    Flashcard("88", "What does operations documentation cover?", "Startup, shutdown, schedules, backups, recovery, security, monitoring, and routine operating procedures.", "8 · Implementation"),
    Flashcard("89", "What supports successful implementation?", "Management support, user involvement, clear communication, training, realistic planning, testing, and change management.", "8 · Implementation"),
    Flashcard("90", "Core implementation security principles?", "Confidentiality, integrity, availability, authentication, authorisation, accountability, least privilege, backup, and recovery.", "8 · Implementation"),

    Flashcard("91", "What should a migration plan contain?", "Conversion approach, tasks, schedule, responsibilities, data migration, testing, training, support, fallback, and contingency steps.", "9 · Transition"),
    Flashcard("92", "What is direct conversion?", "Old system stops and new system starts at once. Fast and cheap, but highest risk.", "9 · Transition"),
    Flashcard("93", "What is parallel conversion?", "Old and new systems run together temporarily. Lowest operational risk, but costly and demanding.", "9 · Transition"),
    Flashcard("94", "What is pilot conversion?", "New system starts at one location or group before wider rollout.", "9 · Transition"),
    Flashcard("95", "What is phased conversion?", "System is introduced gradually by module, feature, location, or user group.", "9 · Transition"),
    Flashcard("96", "How are conversion strategies evaluated?", "Risk, cost, and time, plus system criticality and organisational readiness.", "9 · Transition"),
    Flashcard("97", "What is a business contingency plan?", "Documented response for maintaining or restoring critical operations if conversion or technology fails.", "9 · Transition"),
    Flashcard("98", "Why do users resist system change?", "Fear of job loss, uncertainty, altered routines, loss of status or control, poor communication, and inadequate skills.", "9 · Transition"),
    Flashcard("99", "How can adoption be improved?", "Early involvement, clear benefits, management support, incentives, communication, training, practice, and accessible support.", "9 · Transition"),

    Flashcard("100", "What is corrective maintenance?", "Fixing faults discovered after system deployment.", "10 · Maintenance"),
    Flashcard("101", "What is adaptive maintenance?", "Changing system to work with new business rules, platforms, interfaces, laws, or environments.", "10 · Maintenance"),
    Flashcard("102", "What is perfective maintenance?", "Improving performance, usability, maintainability, or functionality in response to user needs.", "10 · Maintenance"),
    Flashcard("103", "What is preventive maintenance?", "Reducing future failure risk through refactoring, restructuring, documentation, testing, and component replacement.", "10 · Maintenance"),
    Flashcard("104", "How should maintenance requests be controlled?", "Log, classify, prioritise, estimate, approve, assign, test, document, release, and review each request.", "10 · Maintenance"),
    Flashcard("105", "What is configuration management (CM)?", "Controlled identification, tracking, approval, and auditing of system components and changes.", "10 · Maintenance"),
    Flashcard("106", "Why use version control?", "To preserve change history, support collaboration, compare revisions, create releases, and restore known states.", "10 · Maintenance"),
    Flashcard("107", "What makes a sound backup plan?", "Defined scope, frequency, retention, off-site or isolated copies, encryption, ownership, monitoring, and tested restoration.", "10 · Maintenance"),
    Flashcard("108", "Backup vs recovery?", "Backup creates restorable copies. Recovery uses copies and procedures to restore data and service.", "10 · Maintenance"),
    Flashcard("109", "What is system obsolescence?", "State where system no longer meets business, technical, security, support, or economic needs.", "10 · Maintenance"),
    Flashcard("110", "When should a system be replaced rather than maintained?", "When maintenance cost and risk exceed replacement value, technology is unsupported, or system cannot meet essential requirements.", "10 · Maintenance")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(onBack: () -> Unit) {
    val topics = remember { listOf("All") + cbad2103Cards.map { it.category }.distinct() }
    var topic by remember { mutableStateOf("All") }
    val cards = remember(topic) { if (topic == "All") cbad2103Cards else cbad2103Cards.filter { it.category == topic } }
    var currentIndex by remember(topic) { mutableIntStateOf(0) }
    var showAnswer by remember(topic) { mutableStateOf(false) }
    val card = cards[currentIndex]

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("CBAD2103 Flashcards", fontWeight = FontWeight.Bold)
                        Text("System Analysis and Design", style = MaterialTheme.typography.labelMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(topics) { item ->
                    FilterChip(
                        selected = topic == item,
                        onClick = { topic = item },
                        label = { Text(item) }
                    )
                }
            }
            Text(
                "${card.category}  •  ${currentIndex + 1} / ${cards.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
            LinearProgressIndicator(
                progress = { (currentIndex + 1f) / cards.size },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Card(
                onClick = { showAnswer = !showAnswer },
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        if (showAnswer) "ANSWER" else "QUESTION",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(
                        if (showAnswer) card.answer else card.question,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = if (showAnswer) FontWeight.Normal else FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(18.dp))
                    Text(
                        if (showAnswer) "Tap to show question" else "Tap to reveal answer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { currentIndex--; showAnswer = false },
                    enabled = currentIndex > 0,
                    modifier = Modifier.weight(1f)
                ) { Text("Previous") }
                Button(
                    onClick = { currentIndex++; showAnswer = false },
                    enabled = currentIndex < cards.lastIndex,
                    modifier = Modifier.weight(1f)
                ) { Text("Next") }
            }
        }
    }
}

internal fun cbad2103FlashcardCount(): Int = cbad2103Cards.size
internal fun cbad2103TopicCount(): Int = cbad2103Cards.map { it.category }.distinct().size
internal fun cbad2103CardsAreValid(): Boolean = cbad2103Cards.map { it.id }.distinct().size == cbad2103Cards.size &&
    cbad2103Cards.all { it.question.isNotBlank() && it.answer.isNotBlank() && it.category.isNotBlank() }
