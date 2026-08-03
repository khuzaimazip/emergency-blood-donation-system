package EmergencyBloodDonation;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

public class EmergencyBloodDonation extends JFrame {
    private static final String[] BLOOD_GROUPS = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
    private static final String ADMIN_USERNAME = "Khuzaima";
    private static final String ADMIN_PASSWORD = "Khuzaima@123";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Color PAGE_BG = new Color(248, 250, 252);
    private static final Color PANEL_BG = new Color(255, 255, 255);
    private static final Color INK = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color PRIMARY = new Color(190, 24, 93);
    private static final Color PRIMARY_DARK = new Color(76, 5, 35);
    private static final Color ACCENT = new Color(244, 63, 94);
    private static final Color SECONDARY = new Color(15, 118, 110);
    private static final Color SECONDARY_DARK = new Color(17, 94, 89);
    private static final Color DANGER = new Color(220, 38, 38);
    private static final Color DANGER_DARK = new Color(153, 27, 27);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color SOFT_RED = new Color(255, 228, 230);
    private static final Color TABLE_STRIPE = new Color(248, 250, 252);
    private static final Color SELECTION = new Color(255, 228, 230);
    private static final int ANIMATION_DELAY_MS = 11;
    private static final int NAV_COLLAPSED_WIDTH = 64;
    private static final int NAV_EXPANDED_WIDTH = 230;
    private static final String MYSQL_URL_TEMPLATE = "jdbc:mysql://localhost:3306/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String MYSQL_ADMIN_URL = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASSWORD = "";
    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final Map<String, Color> BLOOD_COLORS = createBloodColors();

    private Database db;
    private String currentUser = "";
    private boolean loggedIn = false;
    private boolean adminAuthenticated;
    private CardLayout rootLayout;
    private JPanel rootCards;
    private CardLayout contentLayout;
    private JPanel contentCards;
    private JPanel sideNav;
    private final List<NavButton> navButtons = new ArrayList<>();
    private String currentSection = "Login";
    private final AnnouncementBanner announcementBanner = new AnnouncementBanner();
    private final BloodStockGraphPanel stockGraph = new BloodStockGraphPanel();
    private final JTextField adminAnnouncementField = new JTextField();
    private javax.swing.Timer announcementTimer;
    private javax.swing.Timer uiAnimationTimer;
    private javax.swing.Timer navAnimationTimer;
    private double announcementX;
    private long lastAnnouncementFrameTime;
    private long lastUiFrameTime;
    private long lastNavFrameTime;
    private double animationPhase;
    private double loginBackgroundPhase;
    private double loginCardOpacity = 0.0;
    private double sideNavWidth = NAV_COLLAPSED_WIDTH;
    private int sideNavTargetWidth = NAV_COLLAPSED_WIDTH;

    private final JTextField donorNameField = new JTextField();
    private final JTextField donorPhoneField = new JTextField();
    private final JTextField donorAgeField = new JTextField();
    private final JComboBox<String> donorGenderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
    private final JComboBox<String> donorBloodBox = new JComboBox<>(BLOOD_GROUPS);
    private final JTextField donorAreaField = new JTextField();
    private final JTextField donorCityField = new JTextField();
    private final JTextField donorDonationUnitsField = new JTextField("0");
    private final JCheckBox donorAvailableBox = new JCheckBox("Available for emergency calls", true);

    private final JComboBox<String> stockBloodBox = new JComboBox<>(BLOOD_GROUPS);
    private final JTextField stockUnitsField = new JTextField();
    private final JTextField stockHospitalField = new JTextField();
    private final JTextField stockAreaField = new JTextField();

    private final JTextField requestPatientField = new JTextField();
    private final JComboBox<String> requestBloodBox = new JComboBox<>(BLOOD_GROUPS);
    private final JTextField requestUnitsField = new JTextField();
    private final JTextField requestHospitalField = new JTextField();
    private final JTextField requestAreaField = new JTextField();
    private final JComboBox<String> requestPriorityBox = new JComboBox<>(new String[]{"Critical", "High", "Normal"});

    private final JComboBox<String> distributionRequestBox = new JComboBox<>();
    private final JComboBox<String> distributionBloodBox = new JComboBox<>(BLOOD_GROUPS);
    private final JTextField distributionUnitsField = new JTextField();
    private final JLabel fulfilmentRequestDetailsLabel = new JLabel("Select an emergency request.");
    private final JLabel fulfilmentStockLabel = new JLabel("Available stock: 0");

    private final JComboBox<String> adminRequestBox = new JComboBox<>();
    private final JComboBox<String> adminPriorityBox = new JComboBox<>(new String[]{"Critical", "High", "Normal"});
    private final JComboBox<String> adminDonorBox = new JComboBox<>();
    private final JComboBox<String> adminStockBloodBox = new JComboBox<>(BLOOD_GROUPS);
    private final JTextField adminStockUnitsField = new JTextField();
    private final JTextField adminHighPriorityAreaField = new JTextField();

    private final DefaultTableModel donorModel = tableModel("ID", "Name", "Phone", "Age", "Gender", "Blood", "Area", "City", "Available");
    private final DefaultTableModel stockModel = tableModel("Blood Group", "Units");
    private final DefaultTableModel requestModel = tableModel("ID", "Patient", "Blood", "Needed", "Distributed", "Remaining", "Hospital", "Area", "Priority", "Status");
    private final DefaultTableModel areaModel = tableModel("Area", "High Priority", "Blood Group", "Needed Units");
    private final DefaultTableModel hospitalModel = tableModel("Hospital", "Blood Group", "Distributed Units");
    private final DefaultTableModel transactionModel = tableModel("Time", "Type", "Blood", "Units", "Hospital/Source", "Area", "Notes");
    private final DefaultTableModel highPriorityAreaModel = tableModel("High Priority Area");

    public EmergencyBloodDonation() {
        super("Emergency Blood Donation System");
        configureLook();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(null);
        setContentPane(buildContent());
    }

    private void configureLook() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        UIManager.put("Panel.background", PAGE_BG);
        UIManager.put("TabbedPane.selected", PANEL_BG);
        UIManager.put("TabbedPane.contentAreaColor", PAGE_BG);
        UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
        UIManager.put("Table.selectionBackground", SELECTION);
        UIManager.put("Table.selectionForeground", INK);
        UIManager.put("TextField.caretForeground", PRIMARY);
        UIManager.put("ComboBox.selectionBackground", SELECTION);
    }

    private JComponent buildContent() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PAGE_BG);

        JPanel header = new GradientPanel(new BorderLayout(), PRIMARY_DARK, PRIMARY);
        JLabel title = new JLabel("Emergency Blood Donation System");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 25f));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(14, 18, 2, 18));
        JLabel subtitle = new JLabel("Live donor registry, request fulfilment, and blood stock visibility");
        subtitle.setForeground(new Color(255, 228, 230));
        subtitle.setBorder(new EmptyBorder(0, 19, 12, 18));
        JPanel titleBlock = new JPanel(new BorderLayout());
        titleBlock.setOpaque(false);
        titleBlock.add(title, BorderLayout.NORTH);
        titleBlock.add(subtitle, BorderLayout.SOUTH);
        header.add(titleBlock, BorderLayout.NORTH);
        header.add(announcementBanner, BorderLayout.SOUTH);

        contentLayout = new CardLayout();
        contentCards = new JPanel(contentLayout);
        contentCards.add(dashboardPanel(), "Dashboard");
        contentCards.add(donorPanel(), "Donor Registration");
        contentCards.add(stockPanel(), "Blood Stock");
        contentCards.add(requestPanel(), "Emergency Requests");
        contentCards.add(emergencyFulfilmentPanel(), "Emergency Fulfilment");
        contentCards.add(distributionHistoryPanel(), "Distribution History");
        contentCards.add(reportsPanel(), "Reports");
        contentCards.add(adminPanel(), "Admin");

        sideNav = buildSideNav();
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(PAGE_BG);
        workspace.add(sideNav, BorderLayout.WEST);
        workspace.add(contentCards, BorderLayout.CENTER);

        JPanel appRoot = new JPanel(new BorderLayout());
        appRoot.add(header, BorderLayout.NORTH);
        appRoot.add(workspace, BorderLayout.CENTER);

        rootLayout = new CardLayout();
        rootCards = new JPanel(rootLayout);
        rootCards.add(loginPanel(), "Login");
        rootCards.add(appRoot, "App");

        root.add(rootCards, BorderLayout.CENTER);
        showRoot("Login");
        startAnnouncementSlider();
        startUiAnimation();
        return root;
    }

    private JPanel buildSideNav() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(PRIMARY_DARK);
        nav.setBorder(new EmptyBorder(12, 8, 12, 8));
        nav.setPreferredSize(new Dimension(NAV_COLLAPSED_WIDTH, 100));
        nav.setMinimumSize(new Dimension(NAV_COLLAPSED_WIDTH, 100));

        MouseAdapter hoverListener = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { setSideNavExpanded(true); }
            @Override public void mouseExited(MouseEvent e) {
                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), nav);
                if (!nav.contains(p)) setSideNavExpanded(false);
            }
        };
        nav.addMouseListener(hoverListener);

        addNavButton(nav, hoverListener, "Dashboard", 0);
        addNavButton(nav, hoverListener, "Donor Registration", 1);
        addNavButton(nav, hoverListener, "Blood Stock", 2);
        addNavButton(nav, hoverListener, "Emergency Requests", 3);
        addNavButton(nav, hoverListener, "Emergency Fulfilment", 4);
        addNavButton(nav, hoverListener, "Distribution History", 5);
        addNavButton(nav, hoverListener, "Reports", 6);
        nav.add(Box.createVerticalGlue());
        addNavButton(nav, hoverListener, "Admin", 7);
        return nav;
    }

    private void addNavButton(JPanel nav, MouseAdapter hoverListener, String section, int iconType) {
        NavButton btn = new NavButton(section, new NavIcon(iconType));
        btn.addMouseListener(hoverListener);
        btn.addActionListener(e -> navigateTo(section));
        navButtons.add(btn);
        nav.add(btn);
        nav.add(Box.createVerticalStrut(8));
    }

    private void navigateTo(String section) {
        if (!loggedIn) {
            showRoot("Login");
            return;
        }
        if ("Admin".equals(section) && !adminAuthenticated && !loginAdmin()) return;
        if ("Admin".equals(section)) adminAuthenticated = true;
        showSection(section);
    }

    private void showSection(String section) {
        currentSection = section;
        contentLayout.show(contentCards, section);
        for (NavButton btn : navButtons) btn.setSelected(btn.getSection().equals(section));
    }

    private void setSideNavExpanded(boolean expanded) {
        sideNavTargetWidth = expanded ? NAV_EXPANDED_WIDTH : NAV_COLLAPSED_WIDTH;
        for (NavButton btn : navButtons) btn.setExpanded(expanded);
        if (navAnimationTimer != null && navAnimationTimer.isRunning()) return;
        lastNavFrameTime = System.nanoTime();
        navAnimationTimer = new javax.swing.Timer(ANIMATION_DELAY_MS, e -> animateSideNav());
        navAnimationTimer.start();
    }

    private void animateSideNav() {
        long now = System.nanoTime();
        double elapsed = (now - lastNavFrameTime) / 1_000_000_000.0;
        lastNavFrameTime = now;
        double s = 1 - Math.exp(-elapsed * 18.0);
        sideNavWidth += (sideNavTargetWidth - sideNavWidth) * s;
        if (Math.abs(sideNavTargetWidth - sideNavWidth) < 0.6) {
            sideNavWidth = sideNavTargetWidth;
            navAnimationTimer.stop();
        }
        int w = (int) Math.round(sideNavWidth);
        sideNav.setPreferredSize(new Dimension(w, sideNav.getHeight()));
        sideNav.setMinimumSize(new Dimension(w, 100));
        sideNav.revalidate();
        sideNav.repaint();
    }

    // ─── Panels ──────────────────────────────────────────────────────────────

    private JPanel dashboardPanel() {
        JPanel panel = paddedPanel(new BorderLayout(14, 14));
        JPanel stats = new JPanel(new GridLayout(1, 3, 14, 14));
        stats.setOpaque(false);
        stats.add(summaryCard("Registered Donors", () -> String.valueOf(db == null ? 0 : db.donors.size())));
        stats.add(summaryCard("Total Stock Units", () -> String.valueOf(db == null ? 0 : db.totalStock())));
        stats.add(summaryCard("Open Emergency Requests", () -> String.valueOf(db == null ? 0 : db.openRequests())));

        JTable stockTable = new JTable(stockModel);
        JTable requestsTable = new JTable(requestModel);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                scroll("Current Blood Stock", stockTable),
                scroll("Emergency Request Queue", requestsTable));
        split.setResizeWeight(0.35);
        split.setBorder(null);

        JPanel center = new JPanel(new BorderLayout(14, 14));
        center.setOpaque(false);
        center.add(stockGraph, BorderLayout.NORTH);
        center.add(split, BorderLayout.CENTER);

        panel.add(stats, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel donorPanel() {
        JPanel panel = paddedPanel(new BorderLayout(12, 12));
        JPanel form = formPanel();
        addRow(form, "Name", donorNameField);
        addRow(form, "Phone", donorPhoneField);
        addRow(form, "Age", donorAgeField);
        addRow(form, "Gender", donorGenderBox);
        addRow(form, "Blood Group", donorBloodBox);
        addRow(form, "Area", donorAreaField);
        addRow(form, "City", donorCityField);
        addRow(form, "Donate Units Now", donorDonationUnitsField);
        addRow(form, "", donorAvailableBox);
        JButton reg = new JButton("Register Donor");
        reg.addActionListener(this::registerDonor);
        JButton clr = new JButton("Clear Form");
        clr.addActionListener(e -> clearDonorForm());
        form.add(buttonRow(reg, clr));
        panel.add(form, BorderLayout.WEST);
        panel.add(scroll("Registered Donors", new JTable(donorModel)), BorderLayout.CENTER);
        return panel;
    }

    private JPanel stockPanel() {
        JPanel panel = paddedPanel(new BorderLayout(12, 12));
        JPanel form = formPanel();
        addRow(form, "Blood Group", stockBloodBox);
        addRow(form, "Units", stockUnitsField);
        addRow(form, "Hospital / Blood Bank", stockHospitalField);
        addRow(form, "Area", stockAreaField);
        JButton add = new JButton("Add Stock");
        add.addActionListener(this::addStock);
        form.add(buttonRow(add));
        panel.add(form, BorderLayout.WEST);
        panel.add(scroll("Stock by Blood Group", new JTable(stockModel)), BorderLayout.CENTER);
        return panel;
    }

    private JPanel requestPanel() {
        JPanel panel = paddedPanel(new BorderLayout(12, 12));
        JPanel form = formPanel();
        addRow(form, "Patient Name", requestPatientField);
        addRow(form, "Blood Group", requestBloodBox);
        addRow(form, "Units Needed", requestUnitsField);
        addRow(form, "Hospital", requestHospitalField);
        addRow(form, "Area", requestAreaField);
        addRow(form, "Priority", requestPriorityBox);
        JButton add = new JButton("Create Emergency Request");
        add.addActionListener(this::createRequest);
        form.add(buttonRow(add));
        panel.add(form, BorderLayout.WEST);
        panel.add(scroll("Emergency Requests", new JTable(requestModel)), BorderLayout.CENTER);
        return panel;
    }

    private JPanel emergencyFulfilmentPanel() {
        JPanel panel = paddedPanel(new BorderLayout(12, 12));
        JPanel form = formPanel();
        addRow(form, "Request", distributionRequestBox);
        addRow(form, "Blood Group Given", distributionBloodBox);
        addRow(form, "Units to Distribute", distributionUnitsField);
        fulfilmentRequestDetailsLabel.setBorder(new EmptyBorder(8, 0, 8, 0));
        fulfilmentStockLabel.setBorder(new EmptyBorder(0, 0, 8, 0));
        form.add(fulfilmentRequestDetailsLabel);
        form.add(fulfilmentStockLabel);
        distributionRequestBox.addActionListener(e -> syncDistributionBloodGroup());
        distributionBloodBox.addActionListener(e -> updateFulfilmentDetails());
        JButton dist = new JButton("Fulfil Request");
        dist.addActionListener(this::distributeBlood);
        JButton auto = new JButton("Auto Fill Remaining");
        auto.addActionListener(e -> autoFillDistribution());
        form.add(buttonRow(dist, auto));
        panel.add(form, BorderLayout.WEST);
        panel.add(scroll("Open Emergency Requests", new JTable(requestModel)), BorderLayout.CENTER);
        return panel;
    }

    private JPanel distributionHistoryPanel() {
        JPanel panel = paddedPanel(new BorderLayout(12, 12));
        panel.add(scroll("Stock and Distribution Transactions", new JTable(transactionModel)), BorderLayout.CENTER);
        return panel;
    }

    private JPanel reportsPanel() {
        JPanel panel = paddedPanel(new GridLayout(1, 2, 12, 12));
        panel.add(scroll("Area-wise Pending Need", new JTable(areaModel)));
        panel.add(scroll("Hospital-wise Distribution", new JTable(hospitalModel)));
        return panel;
    }

    private JPanel loginPanel() {
        // Dark gradient background panel
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(20, 20, 30), getWidth(), getHeight(), new Color(80, 35, 20));
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.anchor = GridBagConstraints.CENTER;

        // Left panel with login form
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(380, 600));

        GridBagConstraints leftGbc = new GridBagConstraints();
        leftGbc.gridx = 0;
        leftGbc.gridwidth = 2;
        leftGbc.insets = new Insets(0, 0, 40, 0);
        leftGbc.fill = GridBagConstraints.HORIZONTAL;

        // Blood drop icon simulation
        JPanel dropIcon = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 30, 80));
                // Draw blood drop shape
                int cx = getWidth() / 2, cy = getHeight() / 2;
                int[] xs = {cx, cx - 15, cx - 20, cx, cx + 20, cx + 15};
                int[] ys = {cy - 25, cy - 5, cy + 15, cy + 30, cy + 15, cy - 5};
                g2.fillPolygon(xs, ys, 6);
            }
        };
        dropIcon.setOpaque(false);
        dropIcon.setPreferredSize(new Dimension(80, 80));
        leftPanel.add(dropIcon, leftGbc);

        leftGbc.gridy++;
        leftGbc.insets = new Insets(0, 0, 12, 0);
        JLabel mainTitle = new JLabel("SAVE LIVES");
        mainTitle.setFont(mainTitle.getFont().deriveFont(Font.BOLD, 42f));
        mainTitle.setForeground(Color.WHITE);
        mainTitle.setHorizontalAlignment(SwingConstants.CENTER);
        leftPanel.add(mainTitle, leftGbc);

        leftGbc.gridy++;
        leftGbc.insets = new Insets(0, 0, 36, 0);
        JLabel subtitle = new JLabel("Enter your credentials");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 14f));
        subtitle.setForeground(new Color(200, 200, 200));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        leftPanel.add(subtitle, leftGbc);

        // Username field
        leftGbc.gridy++;
        leftGbc.insets = new Insets(0, 0, 18, 0);
        JTextField userField = new JTextField();
        userField.setPreferredSize(new Dimension(260, 48));
        userField.setFont(userField.getFont().deriveFont(15f));
        userField.setBackground(new Color(40, 40, 50));
        userField.setForeground(Color.WHITE);
        userField.setCaretColor(new Color(255, 50, 120));
        userField.setBorder(new LineBorder(new Color(220, 30, 80), 2, false));
        userField.setText("Username");
        userField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (userField.getText().equals("Username")) userField.setText("");
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (userField.getText().isEmpty()) userField.setText("Username");
            }
        });
        leftPanel.add(userField, leftGbc);

        // Password field
        leftGbc.gridy++;
        leftGbc.insets = new Insets(0, 0, 32, 0);
        JPasswordField passField = new JPasswordField();
        passField.setPreferredSize(new Dimension(260, 48));
        passField.setFont(passField.getFont().deriveFont(15f));
        passField.setBackground(new Color(40, 40, 50));
        passField.setForeground(Color.WHITE);
        passField.setCaretColor(new Color(255, 50, 120));
        passField.setBorder(new LineBorder(new Color(220, 30, 80), 2, false));
        leftPanel.add(passField, leftGbc);

        // Login button
        leftGbc.gridy++;
        leftGbc.insets = new Insets(0, 0, 18, 0);
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setPreferredSize(new Dimension(140, 48));
        loginBtn.setFont(loginBtn.getFont().deriveFont(Font.BOLD, 16f));
        loginBtn.setBackground(new Color(255, 50, 120));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorder(null);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { loginBtn.setBackground(new Color(255, 80, 150)); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { loginBtn.setBackground(new Color(255, 50, 120)); }
        });
        leftPanel.add(loginBtn, leftGbc);

        // Register button
        leftGbc.gridy++;
        leftGbc.insets = new Insets(0, 0, 0, 0);
        JButton registerBtn = new JButton("CREATE ACCOUNT");
        registerBtn.setPreferredSize(new Dimension(140, 48));
        registerBtn.setFont(registerBtn.getFont().deriveFont(Font.BOLD, 16f));
        registerBtn.setBackground(new Color(255, 50, 120));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setFocusPainted(false);
        registerBtn.setBorder(null);
        registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { registerBtn.setBackground(new Color(255, 80, 150)); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { registerBtn.setBackground(new Color(255, 50, 120)); }
        });
        leftPanel.add(registerBtn, leftGbc);

        loginBtn.addActionListener(e -> doLogin(userField.getText().trim(), new String(passField.getPassword())));
        registerBtn.addActionListener(e -> doRegister(userField.getText().trim(), new String(passField.getPassword())));

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(leftPanel, gbc);

        // Right panel with branding/illustration
        JPanel rightPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 15));
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw "BLOOD DONATION" text
                g2.setColor(new Color(255, 255, 255, 40));
                g2.setFont(new Font("Arial", Font.BOLD, 48));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth("BLOOD")) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 - 20;
                g2.drawString("BLOOD", x, y);
                g2.setFont(new Font("Arial", Font.BOLD, 44));
                g2.setColor(new Color(255, 50, 120, 80));
                x = (getWidth() - fm.stringWidth("DONATION")) / 2;
                g2.drawString("DONATION", x, y + 60);
            }
        };
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(480, 600));
        gbc.gridx = 1;
        panel.add(rightPanel, gbc);

        return panel;
    }

    private void doLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password.");
            return;
        }
        try {
            AuthenticationManager auth = new AuthenticationManager();
            auth.initMaster();
            String dbName = auth.authenticate(username, password);
            if (dbName == null) {
                showError("Invalid username or password.");
                return;
            }
            currentUser = username;
            db = new Database(dbName);
            loggedIn = true;
            adminAuthenticated = false;
            setTitle("Emergency Blood Donation System - " + currentUser);
            refreshAll();
            showApp();
            showInfo("Welcome, " + currentUser + ".");
        } catch (Exception ex) {
            showError("Login failed: " + ex.getMessage());
        }
    }

    private void doRegister(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password.");
            return;
        }
        try {
            AuthenticationManager auth = new AuthenticationManager();
            auth.initMaster();
            auth.registerUser(username, password);
            showInfo("Account created. You can now log in.");
        } catch (Exception ex) {
            showError("Registration failed: " + ex.getMessage());
        }
    }

    private JPanel adminPanel() {
        JPanel panel = paddedPanel(new BorderLayout(12, 12));
        JLabel userLabel = new JLabel("Admin user: " + currentUser);
        userLabel.setForeground(SECONDARY_DARK);
        userLabel.setFont(userLabel.getFont().deriveFont(Font.BOLD, 14f));
        panel.add(userLabel, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setOpaque(false);

        JPanel controls = new JPanel(new GridLayout(2, 3, 12, 12));
        controls.setOpaque(false);
        controls.add(adminRequestPanel());
        controls.add(adminDonorPanel());
        controls.add(adminStockPanel());
        controls.add(adminAreaPanel());
        controls.add(adminAnnouncementPanel());
        controls.add(adminPasswordPanel());

        JTabbedPane tables = new JTabbedPane();
        tables.addTab("Requests", scroll("All Emergency Requests", new JTable(requestModel)));
        tables.addTab("Donors", scroll("Registered Donors", new JTable(donorModel)));
        tables.addTab("Stock", scroll("Current Stock", new JTable(stockModel)));
        tables.addTab("High Priority Areas", scroll("Marked Areas", new JTable(highPriorityAreaModel)));
        tables.setPreferredSize(new Dimension(900, 360));

        content.add(controls, BorderLayout.NORTH);
        content.add(tables, BorderLayout.CENTER);

        JScrollPane adminScroll = new JScrollPane(content);
        adminScroll.setBorder(null);
        adminScroll.getViewport().setBackground(PAGE_BG);
        adminScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        adminScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        enableSmoothScrolling(adminScroll);
        panel.add(adminScroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel adminAnnouncementPanel() {
        JPanel panel = formPanel();
        panel.setPreferredSize(new Dimension(330, 240));
        panel.setBorder(BorderFactory.createCompoundBorder(titledBorder("Headline Announcement"), new EmptyBorder(10, 10, 10, 10)));
        addRow(panel, "Headline", adminAnnouncementField);
        JButton save = new JButton("Save Headline");
        save.addActionListener(this::saveAnnouncement);
        JButton clr = new JButton("Clear");
        clr.addActionListener(this::clearAnnouncement);
        panel.add(buttonRow(save, clr));
        return panel;
    }

    private JPanel adminRequestPanel() {
        JPanel panel = formPanel();
        panel.setPreferredSize(new Dimension(330, 240));
        panel.setBorder(BorderFactory.createCompoundBorder(titledBorder("Manage Requests"), new EmptyBorder(10, 10, 10, 10)));
        addRow(panel, "Request", adminRequestBox);
        addRow(panel, "Priority", adminPriorityBox);
        JButton chg = new JButton("Change Priority");
        chg.addActionListener(this::changeRequestPriority);
        JButton del = new JButton("Delete Request");
        del.addActionListener(this::deleteEmergencyRequest);
        panel.add(buttonRow(chg, del));
        return panel;
    }

    private JPanel adminDonorPanel() {
        JPanel panel = formPanel();
        panel.setPreferredSize(new Dimension(330, 240));
        panel.setBorder(BorderFactory.createCompoundBorder(titledBorder("Manage Donors"), new EmptyBorder(10, 10, 10, 10)));
        addRow(panel, "Donor", adminDonorBox);
        JButton del = new JButton("Delete Donor");
        del.addActionListener(this::deleteRegisteredDonor);
        panel.add(buttonRow(del));
        return panel;
    }

    private JPanel adminStockPanel() {
        JPanel panel = formPanel();
        panel.setPreferredSize(new Dimension(330, 240));
        panel.setBorder(BorderFactory.createCompoundBorder(titledBorder("Manage Stock"), new EmptyBorder(10, 10, 10, 10)));
        addRow(panel, "Blood Group", adminStockBloodBox);
        addRow(panel, "Units", adminStockUnitsField);
        JButton rem = new JButton("Remove Units");
        rem.addActionListener(this::removeStockUnits);
        JButton clrStk = new JButton("Clear Group Stock");
        clrStk.addActionListener(this::clearBloodGroupStock);
        panel.add(buttonRow(rem, clrStk));
        return panel;
    }

    private JPanel adminAreaPanel() {
        JPanel panel = formPanel();
        panel.setPreferredSize(new Dimension(330, 240));
        panel.setBorder(BorderFactory.createCompoundBorder(titledBorder("High Priority Areas"), new EmptyBorder(10, 10, 10, 10)));
        addRow(panel, "Area", adminHighPriorityAreaField);
        JButton mark = new JButton("Mark High");
        mark.addActionListener(this::markHighPriorityArea);
        JButton unmark = new JButton("Unmark");
        unmark.addActionListener(this::unmarkHighPriorityArea);
        panel.add(buttonRow(mark, unmark));
        return panel;
    }

    private JPanel adminPasswordPanel() {
        JPanel panel = formPanel();
        panel.setPreferredSize(new Dimension(330, 240));
        panel.setBorder(BorderFactory.createCompoundBorder(titledBorder("Account Security"), new EmptyBorder(10, 10, 10, 10)));
        JLabel note = new JLabel("Update password for your admin account.");
        note.setForeground(MUTED);
        panel.add(note);
        panel.add(new JLabel());
        JButton change = new JButton("Change Password");
        change.addActionListener(this::showChangePasswordDialog);
        panel.add(buttonRow(change));
        return panel;
    }

    private void showChangePasswordDialog(ActionEvent e) {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        JPasswordField currentPass = new JPasswordField();
        JPasswordField newPass = new JPasswordField();
        JPasswordField confirmPass = new JPasswordField();
        panel.add(new JLabel("Current Password")); panel.add(currentPass);
        panel.add(new JLabel("New Password")); panel.add(newPass);
        panel.add(new JLabel("Confirm Password")); panel.add(confirmPass);

        int result = JOptionPane.showConfirmDialog(this, panel, "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String current = new String(currentPass.getPassword());
        String next = new String(newPass.getPassword());
        String confirm = new String(confirmPass.getPassword());
        if (current.isEmpty() || next.isEmpty() || confirm.isEmpty()) {
            showError("Please fill all password fields.");
            return;
        }
        if (!next.equals(confirm)) {
            showError("New password and confirmation do not match.");
            return;
        }
        if (next.length() < 6) {
            showError("Use a stronger password with at least 6 characters.");
            return;
        }

        try {
            AuthenticationManager auth = new AuthenticationManager();
            if (auth.authenticate(currentUser, current) == null) {
                showError("Current password is incorrect.");
                return;
            }
            auth.updatePassword(currentUser, next);
            showInfo("Password updated successfully.");
        } catch (Exception ex) {
            showError("Password change failed: " + ex.getMessage());
        }
    }

    // ─── Action Handlers ─────────────────────────────────────────────────────

    private void registerDonor(ActionEvent e) {
        String name = donorNameField.getText().trim();
        String phone = donorPhoneField.getText().trim();
        String area = donorAreaField.getText().trim();
        String city = donorCityField.getText().trim();
        int age = parsePositiveInt(donorAgeField.getText(), "Age");
        int donated = parseNonNegativeInt(donorDonationUnitsField.getText(), "Donate units now");
        if (age == -1 || donated == -1 || anyBlank(name, phone, area, city)) {
            showError("Please fill donor name, phone, valid age, area, city, and donation units.");
            return;
        }
        // Reject numeric-only names (e.g., user entered an integer in the name field)
        if (name.matches("\\d+")) {
            showError("Invalid name. Please put the valid name.");
            return;
        }
        if (age < 18 || age > 65) { showError("Donor age must be between 18 and 65."); return; }

        String bg = selected(donorBloodBox);
        Donor donor = new Donor(db.nextDonorId++, name, phone, age, selected(donorGenderBox), bg, area, city, donorAvailableBox.isSelected());
        db.donors.add(donor);
        db.saveDonor(donor);
        if (donated > 0) {
            int newStock = db.stock.getOrDefault(bg, 0) + donated;
            db.stock.put(bg, newStock);
            db.updateStock(bg, newStock);
            Transaction t = Transaction.stockIn(bg, donated, "Donor: " + name, area).setDonor(donor.id);
            db.transactions.add(t);
            db.saveTransaction(t);
        }
        clearDonorForm();
        refreshAll();
        showInfo(donated > 0 ? "Donor registered and donated blood added to stock." : "Donor registered successfully.");
    }

    private void addStock(ActionEvent e) {
        int units = parsePositiveInt(stockUnitsField.getText(), "Units");
        String hospital = stockHospitalField.getText().trim();
        String area = stockAreaField.getText().trim();
        if (units == -1 || anyBlank(hospital, area)) { showError("Please enter valid units, hospital/source, and area."); return; }

        String bg = selected(stockBloodBox);
        int newStock = db.stock.getOrDefault(bg, 0) + units;
        db.stock.put(bg, newStock);
        db.updateStock(bg, newStock);
        Transaction t = Transaction.stockIn(bg, units, hospital, area);
        db.transactions.add(t);
        db.saveTransaction(t);
        stockUnitsField.setText(""); stockHospitalField.setText(""); stockAreaField.setText("");
        refreshAll();
        showInfo("Stock added successfully.");
    }

    private void createRequest(ActionEvent e) {
        int units = parsePositiveInt(requestUnitsField.getText(), "Units needed");
        String patient = requestPatientField.getText().trim();
        String hospital = requestHospitalField.getText().trim();
        String area = requestAreaField.getText().trim();
        if (units == -1 || anyBlank(patient, hospital, area)) { showError("Please enter patient, valid units, hospital, and area."); return; }

        String priority = selected(requestPriorityBox);
        if (db.highPriorityAreas.contains(normalizeArea(area))) priority = "Critical";
        EmergencyRequest req = new EmergencyRequest(db.nextRequestId++, patient, selected(requestBloodBox), units, hospital, area, priority);
        db.requests.add(req);
        db.saveRequest(req);
        clearRequestForm();
        refreshAll();
        showInfo("Emergency request created.");
    }

    private void changeRequestPriority(ActionEvent e) {
        EmergencyRequest req = selectedAdminRequest();
        if (req == null) { showError("Please select an emergency request."); return; }
        req.priority = selected(adminPriorityBox);
        db.updateRequest(req);
        Transaction t = Transaction.admin("Admin", 0, req.hospital, req.area, "Priority changed for request #" + req.id + " to " + req.priority);
        db.transactions.add(t);
        db.saveTransaction(t);
        refreshAll();
        showInfo("Emergency request priority updated.");
    }

    private void deleteEmergencyRequest(ActionEvent e) {
        EmergencyRequest req = selectedAdminRequest();
        if (req == null) { showError("Please select an emergency request."); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete request #" + req.id + " for " + req.patientName + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        db.requests.remove(req);
        db.deleteRequest(req.id);
        Transaction t = Transaction.admin(req.bloodGroup, 0, req.hospital, req.area, "Deleted emergency request #" + req.id);
        db.transactions.add(t);
        db.saveTransaction(t);
        refreshAll();
        showInfo("Emergency request deleted.");
    }

    private void deleteRegisteredDonor(ActionEvent e) {
        Donor donor = selectedAdminDonor();
        if (donor == null) { showError("Please select a registered donor."); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete donor #" + donor.id + " - " + donor.name + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        db.donors.remove(donor);
        db.deleteDonor(donor.id);
        Transaction t = Transaction.admin(donor.bloodGroup, 0, "Donor Registry", donor.area, "Deleted donor #" + donor.id + " - " + donor.name);
        db.transactions.add(t);
        db.saveTransaction(t);
        refreshAll();
        showInfo("Registered donor deleted.");
    }

    private void removeStockUnits(ActionEvent e) {
        String bg = selected(adminStockBloodBox);
        int units = parsePositiveInt(adminStockUnitsField.getText(), "Units");
        if (units == -1) return;
        int available = db.stock.getOrDefault(bg, 0);
        if (units > available) { showError("Only " + available + " unit(s) of " + bg + " are available."); return; }
        int newStock = available - units;
        db.stock.put(bg, newStock);
        db.updateStock(bg, newStock);
        Transaction t = Transaction.admin(bg, units, "Admin", "Stock", "Removed stock units");
        db.transactions.add(t);
        db.saveTransaction(t);
        adminStockUnitsField.setText("");
        refreshAll();
        showInfo("Stock units removed.");
    }

    private void clearBloodGroupStock(ActionEvent e) {
        String bg = selected(adminStockBloodBox);
        int available = db.stock.getOrDefault(bg, 0);
        if (JOptionPane.showConfirmDialog(this, "Clear all " + available + " unit(s) of " + bg + " stock?", "Confirm Clear Stock", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        db.stock.put(bg, 0);
        db.updateStock(bg, 0);
        Transaction t = Transaction.admin(bg, available, "Admin", "Stock", "Cleared blood group stock");
        db.transactions.add(t);
        db.saveTransaction(t);
        adminStockUnitsField.setText("");
        refreshAll();
        showInfo("Blood group stock cleared.");
    }

    private void markHighPriorityArea(ActionEvent e) {
        String area = adminHighPriorityAreaField.getText().trim();
        if (area.isEmpty()) { showError("Please enter an area name."); return; }
        db.highPriorityAreas.add(normalizeArea(area));
        db.saveHighPriorityArea(normalizeArea(area));
        for (EmergencyRequest req : db.requests)
            if (normalizeArea(req.area).equals(normalizeArea(area)) && req.remainingUnits() > 0) { req.priority = "Critical"; db.updateRequest(req); }
        Transaction t = Transaction.admin("Admin", 0, "Area Priority", area, "Marked high priority area");
        db.transactions.add(t);
        db.saveTransaction(t);
        adminHighPriorityAreaField.setText("");
        refreshAll();
        showInfo("Area marked as high priority.");
    }

    private void unmarkHighPriorityArea(ActionEvent e) {
        String area = adminHighPriorityAreaField.getText().trim();
        if (area.isEmpty()) { showError("Please enter an area name."); return; }
        db.highPriorityAreas.remove(normalizeArea(area));
        db.deleteHighPriorityArea(normalizeArea(area));
        Transaction t = Transaction.admin("Admin", 0, "Area Priority", area, "Unmarked high priority area");
        db.transactions.add(t);
        db.saveTransaction(t);
        adminHighPriorityAreaField.setText("");
        refreshAll();
        showInfo("Area removed from high priority list.");
    }

    private void saveAnnouncement(ActionEvent e) {
        String ann = adminAnnouncementField.getText().trim();
        if (ann.isEmpty()) { showError("Please enter a headline announcement."); return; }
        db.announcement = ann;
        db.saveAnnouncement(ann);
        Transaction t = Transaction.admin("Admin", 0, "Announcement", "System", "Updated headline announcement");
        db.transactions.add(t);
        db.saveTransaction(t);
        refreshAll();
        showInfo("Announcement saved.");
    }

    private void clearAnnouncement(ActionEvent e) {
        db.announcement = "";
        db.saveAnnouncement("");
        Transaction t = Transaction.admin("Admin", 0, "Announcement", "System", "Cleared headline announcement");
        db.transactions.add(t);
        db.saveTransaction(t);
        refreshAll();
        showInfo("Announcement cleared.");
    }

    private void distributeBlood(ActionEvent e) {
        EmergencyRequest req = selectedRequest();
        if (req == null) { showError("Please select an open emergency request."); return; }
        int units = parsePositiveInt(distributionUnitsField.getText(), "Units to distribute");
        if (units == -1) return;

        String givenBg = selected(distributionBloodBox);
        if (!givenBg.equals(req.bloodGroup)) {
            int newStock = db.stock.getOrDefault(givenBg, 0) + units;
            db.stock.put(givenBg, newStock);
            db.updateStock(givenBg, newStock);
            Transaction t = Transaction.stockIn(givenBg, units, req.hospital, req.area, "Mismatched donation for request #" + req.id + " stocked instead of distributed");
            db.transactions.add(t);
            db.saveTransaction(t);
            distributionUnitsField.setText("");
            refreshAll();
            showInfo("Blood group does not match the request. Donation added to " + givenBg + " stock.");
            return;
        }

        int available = db.stock.getOrDefault(req.bloodGroup, 0);
        int remaining = req.remainingUnits();
        if (units > remaining) { showError("This request only needs " + remaining + " more unit(s)."); return; }
        if (units > available) { showError("Only " + available + " unit(s) of " + req.bloodGroup + " are available."); return; }

        int newStock = available - units;
        db.stock.put(req.bloodGroup, newStock);
        db.updateStock(req.bloodGroup, newStock);
        req.distributedUnits += units;
        req.status = req.remainingUnits() == 0 ? "Fulfilled" : "Partially Fulfilled";
        db.updateRequest(req);
        Transaction t = Transaction.distribution(req.bloodGroup, units, req.hospital, req.area, "Request #" + req.id + " for " + req.patientName).setRequest(req.id);
        db.transactions.add(t);
        db.saveTransaction(t);
        distributionUnitsField.setText("");
        refreshAll();
        showInfo("Blood distributed and stock updated.");
    }

    private void autoFillDistribution() {
        EmergencyRequest req = selectedRequest();
        if (req == null) return;
        int available = db.stock.getOrDefault(selected(distributionBloodBox), 0);
        distributionUnitsField.setText(String.valueOf(Math.min(available, req.remainingUnits())));
    }

    private void syncDistributionBloodGroup() {
        EmergencyRequest req = selectedRequest();
        if (req != null) distributionBloodBox.setSelectedItem(req.bloodGroup);
        updateFulfilmentDetails();
    }

    private void updateFulfilmentDetails() {
        EmergencyRequest req = selectedRequest();
        if (req == null) {
            fulfilmentRequestDetailsLabel.setText("Select an emergency request.");
            fulfilmentStockLabel.setText("Available stock: 0");
            return;
        }
        String givenBg = selected(distributionBloodBox);
        int available = db.stock.getOrDefault(givenBg, 0);
        fulfilmentRequestDetailsLabel.setText("Need: " + req.bloodGroup + " | Remaining: " + req.remainingUnits() + " | Hospital: " + req.hospital + " | Area: " + req.area);
        fulfilmentStockLabel.setText("Available " + givenBg + " stock: " + available + " unit(s)");
    }

    // ─── Refresh Methods ──────────────────────────────────────────────────────

    private void refreshAll() {
        refreshDonors(); refreshStock(); refreshRequests(); refreshTransactions();
        refreshAreaNeeds(); refreshHospitalDistribution(); refreshDistributionChoices();
        refreshAdminRequests(); refreshAdminDonors(); refreshHighPriorityAreas();
        refreshAnnouncement(); updateFulfilmentDetails(); repaint();
    }

    private void refreshDonors() {
        donorModel.setRowCount(0);
        for (Donor d : db.donors)
            donorModel.addRow(new Object[]{d.id, d.name, d.phone, d.age, d.gender, d.bloodGroup, d.area, d.city, d.available ? "Yes" : "No"});
    }

    private void refreshStock() {
        stockModel.setRowCount(0);
        for (String bg : BLOOD_GROUPS) stockModel.addRow(new Object[]{bg, db.stock.getOrDefault(bg, 0)});
        stockGraph.setStock(db.stock);
    }

    private void refreshRequests() {
        requestModel.setRowCount(0);
        for (EmergencyRequest r : db.requests)
            requestModel.addRow(new Object[]{r.id, r.patientName, r.bloodGroup, r.neededUnits, r.distributedUnits, r.remainingUnits(), r.hospital, r.area, r.priority, r.status});
    }

    private void refreshTransactions() {
        transactionModel.setRowCount(0);
        for (Transaction t : db.transactions)
            transactionModel.addRow(new Object[]{t.time, t.type, t.bloodGroup, t.units, t.hospitalOrSource, t.area, t.notes});
    }

    private void refreshAreaNeeds() {
        areaModel.setRowCount(0);
        Map<String, Integer> needs = new TreeMap<>();
        for (EmergencyRequest r : db.requests)
            if (r.remainingUnits() > 0) { String k = r.area + "|" + r.bloodGroup; needs.put(k, needs.getOrDefault(k, 0) + r.remainingUnits()); }
        for (Map.Entry<String, Integer> entry : needs.entrySet()) {
            String[] parts = entry.getKey().split("\\|", 2);
            areaModel.addRow(new Object[]{parts[0], db.highPriorityAreas.contains(normalizeArea(parts[0])) ? "Yes" : "No", parts[1], entry.getValue()});
        }
    }

    private void refreshHospitalDistribution() {
        hospitalModel.setRowCount(0);
        Map<String, Integer> dist = new TreeMap<>();
        for (Transaction t : db.transactions)
            if ("Distribution".equals(t.type)) { String k = t.hospitalOrSource + "|" + t.bloodGroup; dist.put(k, dist.getOrDefault(k, 0) + t.units); }
        for (Map.Entry<String, Integer> entry : dist.entrySet()) {
            String[] parts = entry.getKey().split("\\|", 2);
            hospitalModel.addRow(new Object[]{parts[0], parts[1], entry.getValue()});
        }
    }

    private void refreshDistributionChoices() {
        Object sel = distributionRequestBox.getSelectedItem();
        distributionRequestBox.removeAllItems();
        for (EmergencyRequest r : db.requests)
            if (r.remainingUnits() > 0) distributionRequestBox.addItem(r.id + " - " + r.patientName + " - " + r.bloodGroup + " - Remaining " + r.remainingUnits());
        if (sel != null) distributionRequestBox.setSelectedItem(sel);
    }

    private void refreshAdminRequests() {
        Object sel = adminRequestBox.getSelectedItem();
        adminRequestBox.removeAllItems();
        for (EmergencyRequest r : db.requests)
            adminRequestBox.addItem(r.id + " - " + r.patientName + " - " + r.bloodGroup + " - " + r.priority + " - " + r.status);
        if (sel != null) adminRequestBox.setSelectedItem(sel);
    }

    private void refreshAdminDonors() {
        Object sel = adminDonorBox.getSelectedItem();
        adminDonorBox.removeAllItems();
        for (Donor d : db.donors) adminDonorBox.addItem(d.id + " - " + d.name + " - " + d.bloodGroup + " - " + d.area);
        if (sel != null) adminDonorBox.setSelectedItem(sel);
    }

    private void refreshHighPriorityAreas() {
        highPriorityAreaModel.setRowCount(0);
        for (String area : db.highPriorityAreas) highPriorityAreaModel.addRow(new Object[]{area});
    }

    private void refreshAnnouncement() {
        if (!adminAnnouncementField.hasFocus()) adminAnnouncementField.setText(db.announcement);
        updateAnnouncementBanner();
    }

    // ─── Selection Helpers ────────────────────────────────────────────────────

    private EmergencyRequest selectedRequest() {
        Object item = distributionRequestBox.getSelectedItem();
        if (item == null) return null;
        String v = item.toString();
        int id = Integer.parseInt(v.substring(0, v.indexOf(" - ")));
        for (EmergencyRequest r : db.requests) if (r.id == id) return r;
        return null;
    }

    private EmergencyRequest selectedAdminRequest() {
        Object item = adminRequestBox.getSelectedItem();
        if (item == null) return null;
        String v = item.toString();
        int id = Integer.parseInt(v.substring(0, v.indexOf(" - ")));
        for (EmergencyRequest r : db.requests) if (r.id == id) return r;
        return null;
    }

    private Donor selectedAdminDonor() {
        Object item = adminDonorBox.getSelectedItem();
        if (item == null) return null;
        String v = item.toString();
        int id = Integer.parseInt(v.substring(0, v.indexOf(" - ")));
        for (Donor d : db.donors) if (d.id == id) return d;
        return null;
    }

    // ─── Animation & Banner ───────────────────────────────────────────────────

    private void startAnnouncementSlider() {
        if (announcementTimer != null) announcementTimer.stop();
        announcementX = getWidth();
        lastAnnouncementFrameTime = System.nanoTime();
        announcementTimer = new javax.swing.Timer(ANIMATION_DELAY_MS, e -> {
            long now = System.nanoTime();
            double elapsed = (now - lastAnnouncementFrameTime) / 1_000_000_000.0;
            lastAnnouncementFrameTime = now;
            String ann = currentAnnouncementText();
            FontMetrics fm = announcementBanner.getFontMetrics(announcementBanner.getFont());
            int tw = fm.stringWidth(ann);
            int bw = Math.max(announcementBanner.getWidth(), 1);
            if (announcementX == 0 || announcementX > bw) announcementX = bw;
            announcementX -= 92 * elapsed;
            if (announcementX + tw < 0) announcementX = bw;
            updateAnnouncementBanner();
        });
        announcementTimer.start();
    }

    private void startUiAnimation() {
        if (uiAnimationTimer != null) uiAnimationTimer.stop();
        lastUiFrameTime = System.nanoTime();
        uiAnimationTimer = new javax.swing.Timer(ANIMATION_DELAY_MS, e -> {
            long now = System.nanoTime();
            double elapsed = (now - lastUiFrameTime) / 1_000_000_000.0;
            lastUiFrameTime = now;
            animationPhase += elapsed * 2.45;
            loginBackgroundPhase += elapsed * 0.9;
            stockGraph.stepAnimation(elapsed);
            repaint();
        });
        uiAnimationTimer.start();
    }

    private void updateAnnouncementBanner() {
        announcementBanner.setAnnouncement(currentAnnouncementText(), (int) Math.round(announcementX));
    }

    private String currentAnnouncementText() {
        if (db == null) return "Please log in to see announcements.";
        String ann = db.announcement == null ? "" : db.announcement.trim();
        return ann.isEmpty() ? "No emergency announcement" : ann;
    }

    private void showRoot(String card) {
        if (rootCards != null && rootLayout != null) rootLayout.show(rootCards, card);
    }

    private void showApp() {
        if (rootCards != null && rootLayout != null) {
            showSection("Dashboard");
            rootLayout.show(rootCards, "App");
        }
    }

    // ─── Admin Login ──────────────────────────────────────────────────────────

    private boolean loginAdmin() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        JTextField userF = new JTextField(currentUser);
        JPasswordField passF = new JPasswordField();
        panel.add(new JLabel("Username")); panel.add(userF);
        panel.add(new JLabel("Password")); panel.add(passF);
        AuthenticationManager auth = new AuthenticationManager();
        for (int attempt = 1; attempt <= 3; attempt++) {
            int result = JOptionPane.showConfirmDialog(null, panel, "Admin Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) return false;
            String enteredUser = userF.getText().trim();
            String enteredPass = new String(passF.getPassword());
            if (!enteredUser.equals(currentUser)) {
                JOptionPane.showMessageDialog(null, "Use the current logged-in admin username.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                passF.setText("");
                continue;
            }
            try {
                String dbName = auth.authenticate(enteredUser, enteredPass);
                if (dbName != null) return true;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Authentication error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            JOptionPane.showMessageDialog(null, "Invalid admin username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            passF.setText("");
        }
        return false;
    }

    // ─── UI Helpers ───────────────────────────────────────────────────────────

    private String normalizeArea(String area) { return area.trim().toLowerCase(Locale.ROOT); }

    private JPanel summaryCard(String label, ValueProvider provider) {
        JPanel panel = new JPanel(new BorderLayout(4, 6)) {
            @Override protected void paintComponent(Graphics g0) {
                ((JLabel) getComponent(0)).setText(provider.value());
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                float glow = (float) ((Math.sin(animationPhase) + 1) / 2);
                g.setPaint(new GradientPaint(0, 0, PANEL_BG, 0, getHeight(), blend(new Color(255, 251, 252), SOFT_RED, glow * 0.22f)));
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g.setColor(BORDER); g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g.setColor(PRIMARY); g.fillRoundRect(0, 0, 5, getHeight(), 8, 8);
                g.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(18, 20, 18, 18));
        JLabel value = new JLabel(provider.value());
        value.setFont(value.getFont().deriveFont(Font.BOLD, 32f));
        value.setForeground(PRIMARY_DARK);
        JLabel text = new JLabel(label);
        text.setFont(text.getFont().deriveFont(Font.BOLD, 12f));
        text.setForeground(MUTED);
        panel.add(value, BorderLayout.CENTER);
        panel.add(text, BorderLayout.SOUTH);
        return panel;
    }

    private JScrollPane scroll(String title, JTable table) {
        styleTable(table);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        JScrollPane pane = new JScrollPane(table);
        pane.getViewport().setBackground(PANEL_BG);
        pane.setBorder(titledBorder(title));
        enableSmoothScrolling(pane);
        return pane;
    }

    private void enableSmoothScrolling(JScrollPane pane) {
        pane.setWheelScrollingEnabled(false);
        pane.getVerticalScrollBar().setUnitIncrement(18);
        pane.getHorizontalScrollBar().setUnitIncrement(18);
        pane.addMouseWheelListener(new SmoothScrollHandler(pane));
    }

    private JPanel paddedPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(PAGE_BG);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));
        return p;
    }

    private JPanel formPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(310, 100));
        p.setBackground(PANEL_BG);
        p.setBorder(BorderFactory.createCompoundBorder(titledBorder("Entry Form"), new EmptyBorder(10, 10, 10, 10)));
        return p;
    }

    private void addRow(JPanel panel, String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8, 4));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        JLabel lbl = new JLabel(label);
        lbl.setForeground(MUTED);
        lbl.setPreferredSize(new Dimension(110, 24));
        styleInput(field);
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));
        panel.add(row);
    }

    private JPanel buttonRow(JButton... buttons) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        row.setBorder(new EmptyBorder(6, 0, 0, 0));
        if (buttons.length == 1) row.setLayout(new BorderLayout());
        else row.setLayout(new GridLayout(1, buttons.length, 8, 0));
        for (JButton btn : buttons) {
            styleButton(btn);
            if (buttons.length == 1) row.add(btn, BorderLayout.CENTER);
            else row.add(btn);
        }
        return row;
    }

    private void styleInput(JComponent field) {
        field.setFont(field.getFont().deriveFont(13f));
        field.setForeground(INK);
        if (field instanceof JTextField) {
            field.setBackground(Color.WHITE);
            field.setBorder(BorderFactory.createCompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(6, 9, 6, 9)));
        } else if (field instanceof JComboBox) {
            JComboBox<?> box = (JComboBox<?>) field;
            box.setBackground(Color.WHITE);
            box.setOpaque(false);
            box.setBorder(new EmptyBorder(0, 0, 0, 0));
            box.setRenderer(new AnimatedComboRenderer());
            box.setUI(new AnimatedComboBoxUI());
        } else if (field instanceof JCheckBox) {
            field.setOpaque(false);
            field.setForeground(INK);
        }
    }

    private void styleButton(JButton button) {
        boolean danger = isDangerButton(button.getText());
        boolean secondary = isSecondaryButton(button.getText());
        Color base = danger ? DANGER : secondary ? SECONDARY : PRIMARY;
        Color hover = danger ? new Color(239, 68, 68) : secondary ? new Color(20, 184, 166) : ACCENT;
        Color pressed = danger ? DANGER_DARK : secondary ? SECONDARY_DARK : PRIMARY_DARK;
        button.setUI(new BasicButtonUI());
        button.setFocusPainted(false); button.setBorderPainted(false);
        button.setContentAreaFilled(true); button.setOpaque(true);
        button.setForeground(Color.WHITE); button.setBackground(base);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(9, 10, 9, 10));
        button.setFont(button.getFont().deriveFont(Font.BOLD, button.getText().length() > 20 ? 12f : 13f));
        button.setMinimumSize(new Dimension(96, 38)); button.setPreferredSize(new Dimension(120, 38));
        button.addChangeListener(e -> {
            ButtonModel m = button.getModel();
            if (m.isPressed()) button.setBackground(pressed);
            else if (m.isRollover()) button.setBackground(hover);
            else button.setBackground(base);
        });
    }

    private boolean isDangerButton(String t) { String l = t.toLowerCase(Locale.ROOT); return l.contains("delete") || l.contains("remove") || l.contains("clear group"); }
    private boolean isSecondaryButton(String t) { String l = t.toLowerCase(Locale.ROOT); return l.contains("clear") || l.contains("auto") || l.contains("unmark"); }

    private void styleTable(JTable table) {
        table.setRowHeight(28); table.setShowVerticalLines(false);
        table.setGridColor(new Color(226, 232, 240)); table.setForeground(INK);
        table.setSelectionBackground(SELECTION); table.setSelectionForeground(INK);
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(PRIMARY_DARK);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_STRIPE);
                c.setForeground(INK);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    private TitledBorder titledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(new LineBorder(BORDER, 1, true), title);
        border.setTitleColor(PRIMARY_DARK);
        Font base = UIManager.getFont("Label.font");
        if (base == null) base = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        border.setTitleFont(base.deriveFont(Font.BOLD, 13f));
        return border;
    }

    private void clearDonorForm() {
        donorNameField.setText(""); donorPhoneField.setText(""); donorAgeField.setText("");
        donorAreaField.setText(""); donorCityField.setText("");
        donorDonationUnitsField.setText("0"); donorAvailableBox.setSelected(true);
    }

    private void clearRequestForm() {
        requestPatientField.setText(""); requestUnitsField.setText("");
        requestHospitalField.setText(""); requestAreaField.setText("");
    }

    private int parsePositiveInt(String v, String field) {
        try { int n = Integer.parseInt(v.trim()); if (n <= 0) { showError(field + " must be greater than zero."); return -1; } return n; }
        catch (NumberFormatException ex) { showError(field + " must be a valid number."); return -1; }
    }

    private int parseNonNegativeInt(String v, String field) {
        try { int n = Integer.parseInt(v.trim()); if (n < 0) { showError(field + " cannot be negative."); return -1; } return n; }
        catch (NumberFormatException ex) { showError(field + " must be a valid number."); return -1; }
    }

    private String selected(JComboBox<String> box) { return Objects.requireNonNull(box.getSelectedItem()).toString(); }
    private boolean anyBlank(String... vals) { for (String v : vals) if (v == null || v.trim().isEmpty()) return true; return false; }
    private void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE); }
    private void showInfo(String msg) { JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE); }

    private static DefaultTableModel tableModel(String... cols) {
        return new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    }

    private static Map<String, Color> createBloodColors() {
        Map<String, Color> m = new LinkedHashMap<>();
        m.put("A+", new Color(225, 29, 72)); m.put("A-", new Color(244, 63, 94));
        m.put("B+", new Color(234, 88, 12)); m.put("B-", new Color(217, 119, 6));
        m.put("AB+", new Color(147, 51, 234)); m.put("AB-", new Color(79, 70, 229));
        m.put("O+", new Color(13, 148, 136)); m.put("O-", new Color(2, 132, 199));
        return m;
    }

    private static Color blend(Color s, Color e, float amt) {
        float a = Math.max(0f, Math.min(1f, amt));
        return new Color(
            Math.round(s.getRed()   + (e.getRed()   - s.getRed())   * a),
            Math.round(s.getGreen() + (e.getGreen() - s.getGreen()) * a),
            Math.round(s.getBlue()  + (e.getBlue()  - s.getBlue())  * a),
            Math.round(s.getAlpha() + (e.getAlpha() - s.getAlpha()) * a));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EmergencyBloodDonation().setVisible(true));
    }

    // ─── Interfaces & Inner Classes ───────────────────────────────────────────

    private interface ValueProvider { String value(); }

    // ─── Authentication Manager ─────────────────────────────────────────────
    private static class AuthenticationManager {
        private static final String MASTER_DB = "bloodbank_master";

        private Connection adminConn;

        static class AuthResult { final String username; final String dbName; AuthResult(String u, String d) { username = u; dbName = d; } }

        void initMaster() throws SQLException, ClassNotFoundException {
            Class.forName(MYSQL_DRIVER);
            adminConn = DriverManager.getConnection(MYSQL_ADMIN_URL, MYSQL_USER, MYSQL_PASSWORD);
            try (Statement stmt = adminConn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + MASTER_DB + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }
            try (Connection master = DriverManager.getConnection(String.format(MYSQL_URL_TEMPLATE, MASTER_DB), MYSQL_USER, MYSQL_PASSWORD);
                Statement stmt = master.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (username VARCHAR(100) PRIMARY KEY, password_hash TEXT NOT NULL, salt TEXT NOT NULL, db_name VARCHAR(200) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                // If users table empty but legacy 'bloodbank' DB exists, register existing admin using hardcoded creds and point to legacy DB
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
                if (rs.next() && rs.getInt(1) == 0) {
                    if (databaseExists("bloodbank")) {
                        // register default admin using existing hardcoded credentials and point to 'bloodbank' DB
                        registerUserInternal(master, ADMIN_USERNAME, ADMIN_PASSWORD, "bloodbank");
                    }
                }
            }
        }

        private boolean databaseExists(String dbName) throws SQLException {
            try (PreparedStatement ps = adminConn.prepareStatement("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?")) {
                ps.setString(1, dbName);
                try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
            }
        }

        private void registerUserInternal(Connection masterConn, String username, String password, String dbName) throws SQLException {
            try {
                byte[] salt = generateSalt();
                String saltB = Base64.getEncoder().encodeToString(salt);
                String hash = hashPassword(password, salt);
                try (PreparedStatement ps = masterConn.prepareStatement("INSERT INTO users(username,password_hash,salt,db_name) VALUES(?,?,?,?)")) {
                    ps.setString(1, username);
                    ps.setString(2, hash);
                    ps.setString(3, saltB);
                    ps.setString(4, dbName);
                    ps.executeUpdate();
                }
            } catch (Exception ex) { throw new SQLException("Failed to register user: " + ex.getMessage(), ex); }
        }

        public AuthResult showLoginDialog() throws SQLException {
            JPanel panel = new JPanel(new BorderLayout(8,8));
            JPanel fields = new JPanel(new GridLayout(3,2,6,6));
            JTextField userF = new JTextField();
            JPasswordField passF = new JPasswordField();
            fields.add(new JLabel("Username")); fields.add(userF);
            fields.add(new JLabel("Password")); fields.add(passF);
            JButton registerBtn = new JButton("Create Account");
            panel.add(fields, BorderLayout.CENTER);
            panel.add(registerBtn, BorderLayout.SOUTH);

            registerBtn.addActionListener(e -> {
                String u = userF.getText().trim();
                String p = new String(passF.getPassword());
                if (u.isEmpty() || p.isEmpty()) { JOptionPane.showMessageDialog(null, "Please enter username and password."); return; }
                try { String dbName = registerUser(u, p); JOptionPane.showMessageDialog(null, "Account created. Please login."); }
                catch (Exception ex) { JOptionPane.showMessageDialog(null, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
            });

            while (true) {
                int res = JOptionPane.showConfirmDialog(null, panel, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (res != JOptionPane.OK_OPTION) return null;
                String user = userF.getText().trim(); String pass = new String(passF.getPassword());
                if (user.isEmpty() || pass.isEmpty()) { JOptionPane.showMessageDialog(null, "Enter username and password."); continue; }
                try {
                    String dbName = authenticate(user, pass);
                    if (dbName != null) return new AuthResult(user, dbName);
                    JOptionPane.showMessageDialog(null, "Invalid username or password.");
                } catch (Exception ex) { JOptionPane.showMessageDialog(null, "Authentication error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
            }
        }

        public String registerUser(String username, String password) throws SQLException, ClassNotFoundException {
            username = username.trim();
            if (username.isEmpty()) throw new SQLException("Username cannot be empty");
            Class.forName(MYSQL_DRIVER);
            try (Connection master = DriverManager.getConnection(String.format(MYSQL_URL_TEMPLATE, MASTER_DB), MYSQL_USER, MYSQL_PASSWORD)) {
                try (PreparedStatement ps = master.prepareStatement("SELECT username FROM users WHERE username = ?")) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) throw new SQLException("User already exists"); }
                }
                String dbName = "bloodbank_" + username.toLowerCase();
                // create user's database
                try (Statement s = adminConn.createStatement()) { s.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"); }
                // persist user
                registerUserInternal(master, username, password, dbName);
                return dbName;
            }
        }

        public String authenticate(String username, String password) throws SQLException, ClassNotFoundException, InvalidKeySpecException, NoSuchAlgorithmException {
            username = username.trim();
            Class.forName(MYSQL_DRIVER);
            try (Connection master = DriverManager.getConnection(String.format(MYSQL_URL_TEMPLATE, MASTER_DB), MYSQL_USER, MYSQL_PASSWORD);
                PreparedStatement ps = master.prepareStatement("SELECT password_hash,salt,db_name FROM users WHERE username = ?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    String hash = rs.getString(1); String saltB = rs.getString(2); String dbName = rs.getString(3);
                    byte[] salt = Base64.getDecoder().decode(saltB);
                    String tryHash = hashPassword(password, salt);
                    return tryHash.equals(hash) ? dbName : null;
                }
            }
        }

        public void updatePassword(String username, String newPassword) throws SQLException, ClassNotFoundException {
            username = username.trim();
            if (username.isEmpty()) throw new SQLException("Username cannot be empty");
            if (newPassword == null || newPassword.isEmpty()) throw new SQLException("Password cannot be empty");
            Class.forName(MYSQL_DRIVER);
            try (Connection master = DriverManager.getConnection(String.format(MYSQL_URL_TEMPLATE, MASTER_DB), MYSQL_USER, MYSQL_PASSWORD);
                PreparedStatement ps = master.prepareStatement("UPDATE users SET password_hash = ?, salt = ? WHERE username = ?")) {
                byte[] salt = generateSalt();
                String hash = hashPassword(newPassword, salt);
                String saltB = Base64.getEncoder().encodeToString(salt);
                ps.setString(1, hash);
                ps.setString(2, saltB);
                ps.setString(3, username);
                if (ps.executeUpdate() != 1) throw new SQLException("User not found.");
            } catch (Exception ex) {
                if (ex instanceof SQLException) throw (SQLException) ex;
                throw new SQLException("Failed to update password: " + ex.getMessage(), ex);
            }
        }

        private static byte[] generateSalt() { byte[] s = new byte[16]; new SecureRandom().nextBytes(s); return s; }

        private static String hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        }
    }

    // ── MySQL-Persistent Database ────────────────────────────────────────────
    private static class Database {
        int nextDonorId = 1;
        int nextRequestId = 1;
        final List<Donor> donors = new ArrayList<>();
        final List<EmergencyRequest> requests = new ArrayList<>();
        final List<Transaction> transactions = new ArrayList<>();
        final Map<String, Integer> stock = new LinkedHashMap<>();
        Set<String> highPriorityAreas = new TreeSet<>();
        String announcement = "";
        Connection conn;
        String mysqlStatus = "Not connected";
        private final String dbName;

        Database(String dbName) {
            this.dbName = dbName;
            for (String g : BLOOD_GROUPS) stock.put(g, 0);
            connect();
        }

        void connect() {
            try {
                Class.forName(MYSQL_DRIVER);
                String url = String.format(MYSQL_URL_TEMPLATE, dbName);
                conn = DriverManager.getConnection(url, MYSQL_USER, MYSQL_PASSWORD);
                mysqlStatus = "Connected to MySQL (" + dbName + ")";
                createTablesIfNeeded();
                loadAllData();
                System.out.println("MySQL connection established and data loaded.");
            } catch (ClassNotFoundException e) {
                mysqlStatus = "MySQL JDBC driver not found";
                System.err.println("MySQL JDBC driver missing: " + e.getMessage());
            } catch (SQLException e) {
                if (e.getMessage() != null && e.getMessage().contains("Unknown database")) {
                    try {
                        createDatabase(dbName);
                        String url = String.format(MYSQL_URL_TEMPLATE, dbName);
                        conn = DriverManager.getConnection(url, MYSQL_USER, MYSQL_PASSWORD);
                        mysqlStatus = "Connected to MySQL (" + dbName + ")";
                        createTablesIfNeeded();
                        loadAllData();
                        System.out.println("MySQL database created and connection established for: " + dbName);
                    } catch (SQLException inner) {
                        mysqlStatus = "MySQL reconnection failed: " + inner.getMessage();
                        System.err.println("MySQL reconnection failed: " + inner.getMessage());
                    }
                } else {
                    mysqlStatus = "MySQL connection failed: " + e.getMessage();
                    System.err.println("MySQL connection failed: " + e.getMessage());
                }
            }
        }

        private void createDatabase(String name) throws SQLException {
            try (Connection adminConn = DriverManager.getConnection(MYSQL_ADMIN_URL, MYSQL_USER, MYSQL_PASSWORD);
                Statement stmt = adminConn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + name + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                System.out.println("Created database " + name + ".");
            }
        }

        private void createTablesIfNeeded() throws SQLException {
            if (conn == null) return;
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS stock (blood_group VARCHAR(5) PRIMARY KEY, units INT NOT NULL DEFAULT 0)");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS donors (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100) NOT NULL, phone VARCHAR(30) NOT NULL, age INT, gender VARCHAR(10), blood_group VARCHAR(5), area VARCHAR(100), city VARCHAR(100), available BOOLEAN DEFAULT TRUE)");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS requests (id INT PRIMARY KEY AUTO_INCREMENT, patient VARCHAR(100) NOT NULL, blood_group VARCHAR(5), needed_units INT NOT NULL, distributed_units INT DEFAULT 0, hospital VARCHAR(200) NOT NULL, area VARCHAR(100), priority VARCHAR(20) DEFAULT 'Normal', status VARCHAR(20) DEFAULT 'Pending')");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS high_priority_areas (area VARCHAR(100) PRIMARY KEY, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS transactions (id INT PRIMARY KEY AUTO_INCREMENT, donor_id INT, request_id INT, time DATETIME NOT NULL, type VARCHAR(50) NOT NULL, blood_group VARCHAR(5) NOT NULL, units INT NOT NULL, destination VARCHAR(200), area VARCHAR(100), notes VARCHAR(255), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS settings (`key` VARCHAR(100) PRIMARY KEY, `value` TEXT, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");
            }
            ensureTransactionColumns();
            ensureForeignKeys();
        }

        private void ensureTransactionColumns() throws SQLException {
            if (!hasColumn("transactions", "donor_id")) {
                executeUpdate("ALTER TABLE transactions ADD COLUMN donor_id INT NULL");
            }
            if (!hasColumn("transactions", "request_id")) {
                executeUpdate("ALTER TABLE transactions ADD COLUMN request_id INT NULL");
            }
            if (!hasColumn("transactions", "created_at")) {
                executeUpdate("ALTER TABLE transactions ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            }
        }

        private void ensureForeignKeys() throws SQLException {
            ensureStockReferencesExist();

            if (!hasImportedKey("donors", "blood_group")) {
                executeUpdate("ALTER TABLE donors ADD CONSTRAINT fk_donors_stock FOREIGN KEY (blood_group) REFERENCES stock(blood_group) ON DELETE RESTRICT ON UPDATE CASCADE");
            }
            if (!hasImportedKey("requests", "blood_group")) {
                executeUpdate("ALTER TABLE requests ADD CONSTRAINT fk_requests_stock FOREIGN KEY (blood_group) REFERENCES stock(blood_group) ON DELETE RESTRICT ON UPDATE CASCADE");
            }
            if (!hasImportedKey("transactions", "donor_id")) {
                executeUpdate("ALTER TABLE transactions ADD CONSTRAINT fk_transactions_donor FOREIGN KEY (donor_id) REFERENCES donors(id) ON DELETE SET NULL ON UPDATE CASCADE");
            }
            if (!hasImportedKey("transactions", "request_id")) {
                executeUpdate("ALTER TABLE transactions ADD CONSTRAINT fk_transactions_request FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE SET NULL ON UPDATE CASCADE");
            }
            if (!hasImportedKey("transactions", "blood_group")) {
                executeUpdate("ALTER TABLE transactions ADD CONSTRAINT fk_transactions_stock FOREIGN KEY (blood_group) REFERENCES stock(blood_group) ON DELETE RESTRICT ON UPDATE CASCADE");
            }
        }

        private void ensureStockReferencesExist() throws SQLException {
            executeUpdate("INSERT IGNORE INTO stock (blood_group, units) SELECT DISTINCT blood_group, 0 FROM donors WHERE blood_group IS NOT NULL");
            executeUpdate("INSERT IGNORE INTO stock (blood_group, units) SELECT DISTINCT blood_group, 0 FROM requests WHERE blood_group IS NOT NULL");
            executeUpdate("INSERT IGNORE INTO stock (blood_group, units) SELECT DISTINCT blood_group, 0 FROM transactions WHERE blood_group IS NOT NULL");
        }

        private boolean hasColumn(String tableName, String columnName) throws SQLException {
            try (ResultSet cols = conn.getMetaData().getColumns(null, null, tableName, columnName)) {
                return cols.next();
            }
        }

        private boolean hasImportedKey(String tableName, String columnName) throws SQLException {
            try (ResultSet keys = conn.getMetaData().getImportedKeys(null, null, tableName)) {
                while (keys.next()) {
                    if (columnName.equalsIgnoreCase(keys.getString("FKCOLUMN_NAME"))) {
                        return true;
                    }
                }
                return false;
            }
        }

        private void executeUpdate(String sql) throws SQLException {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            }
        }

        private void loadAllData() throws SQLException {
            donors.clear();
            requests.clear();
            transactions.clear();
            stock.clear();
            highPriorityAreas.clear();
            for (String bg : BLOOD_GROUPS) stock.put(bg, 0);

            loadDonors();
            loadRequests();
            loadTransactions();
            loadStock();
            loadHighPriorityAreas();
            loadAnnouncement();
        }

        private void loadDonors() throws SQLException {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id, name, phone, age, gender, blood_group, area, city, available FROM donors ORDER BY id")) {
                while (rs.next()) {
                    donors.add(new Donor(rs.getInt("id"), rs.getString("name"), rs.getString("phone"), rs.getInt("age"),
                        rs.getString("gender"), rs.getString("blood_group"), rs.getString("area"), rs.getString("city"), rs.getBoolean("available")));
                    nextDonorId = Math.max(nextDonorId, rs.getInt("id") + 1);
                }
            }
        }

        private void loadRequests() throws SQLException {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id, patient, blood_group, needed_units, distributed_units, hospital, area, priority, status FROM requests ORDER BY id")) {
                while (rs.next()) {
                    EmergencyRequest req = new EmergencyRequest(rs.getInt("id"), rs.getString("patient"), rs.getString("blood_group"),
                        rs.getInt("needed_units"), rs.getString("hospital"), rs.getString("area"), rs.getString("priority"));
                    req.distributedUnits = rs.getInt("distributed_units");
                    req.status = rs.getString("status");
                    requests.add(req);
                    nextRequestId = Math.max(nextRequestId, rs.getInt("id") + 1);
                }
            }
        }

        private void loadTransactions() throws SQLException {
            try (Statement stmt = conn.createStatement(); ResultSet cnt = stmt.executeQuery("SELECT COUNT(*) FROM transactions")) {
                if (cnt.next()) System.out.println("transactions table contains: " + cnt.getInt(1) + " rows");
            }

            try {
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id, donor_id, request_id, time, type, blood_group, units, destination, area, notes FROM transactions ORDER BY id")) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String time = rs.getString("time");
                        String type = rs.getString("type");
                        String bg = rs.getString("blood_group");
                        int units = rs.getInt("units");
                        String dest = rs.getString("destination");
                        String area = rs.getString("area");
                        String notes = rs.getString("notes");
                        Transaction t = new Transaction(time, type, bg, units, dest, area, notes);
                        int donorId = rs.getInt("donor_id"); if (!rs.wasNull()) t.setDonor(donorId);
                        int requestId = rs.getInt("request_id"); if (!rs.wasNull()) t.setRequest(requestId);
                        transactions.add(t);
                        if (transactions.size() <= 5) System.out.println("Loaded transaction id=" + id + " type=" + type + " bg=" + bg + " units=" + units);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Transaction SELECT with id/donor_id/request_id failed, falling back to legacy schema: " + e.getMessage());
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT time, type, blood_group, units, destination, area, notes FROM transactions ORDER BY time")) {
                    int counter = 0;
                    while (rs.next()) {
                        String time = rs.getString("time");
                        String type = rs.getString("type");
                        String bg = rs.getString("blood_group");
                        int units = rs.getInt("units");
                        String dest = rs.getString("destination");
                        String area = rs.getString("area");
                        String notes = rs.getString("notes");
                        Transaction t = new Transaction(time, type, bg, units, dest, area, notes);
                        transactions.add(t);
                        if (counter < 5) System.out.println("Loaded legacy transaction type=" + type + " bg=" + bg + " units=" + units);
                        counter++;
                    }
                }
            }
        }

        private void loadStock() throws SQLException {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT blood_group, units FROM stock")) {
                while (rs.next()) stock.put(rs.getString("blood_group"), rs.getInt("units"));
            }
            for (String bg : BLOOD_GROUPS) stock.putIfAbsent(bg, 0);
        }

        private void loadHighPriorityAreas() throws SQLException {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT area FROM high_priority_areas")) {
                while (rs.next()) highPriorityAreas.add(rs.getString("area"));
            }
        }

        private void loadAnnouncement() throws SQLException {
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT `value` FROM settings WHERE `key`='announcement'")) {
                if (rs.next()) announcement = rs.getString("value");
            }
        }

        // ─── Methods for saving to MySQL ───
        void saveDonor(Donor donor) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO donors (id, name, phone, age, gender, blood_group, area, city, available) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                pstmt.setInt(1, donor.id); pstmt.setString(2, donor.name); pstmt.setString(3, donor.phone); pstmt.setInt(4, donor.age);
                pstmt.setString(5, donor.gender); pstmt.setString(6, donor.bloodGroup); pstmt.setString(7, donor.area); pstmt.setString(8, donor.city); pstmt.setBoolean(9, donor.available);
                pstmt.executeUpdate();
            } catch (SQLException e) { System.err.println("Error saving donor: " + e.getMessage()); }
        }

        void deleteDonor(int id) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM donors WHERE id = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            } catch (SQLException e) { System.err.println("Error deleting donor: " + e.getMessage()); }
        }

        void saveRequest(EmergencyRequest req) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO requests (id, patient, blood_group, needed_units, distributed_units, hospital, area, priority, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                pstmt.setInt(1, req.id); pstmt.setString(2, req.patientName); pstmt.setString(3, req.bloodGroup); pstmt.setInt(4, req.neededUnits);
                pstmt.setInt(5, req.distributedUnits); pstmt.setString(6, req.hospital); pstmt.setString(7, req.area); pstmt.setString(8, req.priority); pstmt.setString(9, req.status);
                pstmt.executeUpdate();
            } catch (SQLException e) { System.err.println("Error saving request: " + e.getMessage()); }
        }

        void updateRequest(EmergencyRequest req) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement("UPDATE requests SET priority = ?, status = ?, distributed_units = ? WHERE id = ?")) {
                pstmt.setString(1, req.priority); pstmt.setString(2, req.status); pstmt.setInt(3, req.distributedUnits); pstmt.setInt(4, req.id);
                pstmt.executeUpdate();
            } catch (SQLException e) { System.err.println("Error updating request: " + e.getMessage()); }
        }

        void deleteRequest(int id) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM requests WHERE id = ?")) {
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            } catch (SQLException e) { System.err.println("Error deleting request: " + e.getMessage()); }
        }

        void saveTransaction(Transaction t) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO transactions (donor_id, request_id, time, type, blood_group, units, destination, area, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                if (t.donorId > 0) pstmt.setInt(1, t.donorId); else pstmt.setNull(1, java.sql.Types.INTEGER);
                if (t.requestId > 0) pstmt.setInt(2, t.requestId); else pstmt.setNull(2, java.sql.Types.INTEGER);
                pstmt.setString(3, t.time); pstmt.setString(4, t.type); pstmt.setString(5, t.bloodGroup); pstmt.setInt(6, t.units);
                pstmt.setString(7, t.hospitalOrSource); pstmt.setString(8, t.area); pstmt.setString(9, t.notes);
                pstmt.executeUpdate();
            } catch (SQLException e) { System.err.println("Error saving transaction: " + e.getMessage()); }
        }

        void updateStock(String bloodGroup, int units) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement("UPDATE stock SET units = ? WHERE blood_group = ?")) {
                pstmt.setInt(1, units); pstmt.setString(2, bloodGroup);
                int rows = pstmt.executeUpdate();
                if (rows == 0) {
                    try (PreparedStatement insert = conn.prepareStatement("INSERT INTO stock (blood_group, units) VALUES (?, ?)")) {
                        insert.setString(1, bloodGroup); insert.setInt(2, units);
                        insert.executeUpdate();
                    }
                }
            } catch (SQLException e) { System.err.println("Error updating stock: " + e.getMessage()); }
        }

        void saveHighPriorityArea(String area) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT IGNORE INTO high_priority_areas (area) VALUES (?)")) {
                pstmt.setString(1, area);
                pstmt.executeUpdate();
            } catch (SQLException e) { System.err.println("Error saving high priority area: " + e.getMessage()); }
        }

        void deleteHighPriorityArea(String area) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement("DELETE FROM high_priority_areas WHERE area = ?")) {
                pstmt.setString(1, area);
                pstmt.executeUpdate();
            } catch (SQLException e) { System.err.println("Error deleting high priority area: " + e.getMessage()); }
        }

        void saveAnnouncement(String text) {
            if (conn == null) return;
            try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO settings (`key`, `value`) VALUES ('announcement', ?) ON DUPLICATE KEY UPDATE `value`=?")) {
                pstmt.setString(1, text); pstmt.setString(2, text);
                pstmt.executeUpdate();
            } catch (SQLException e) { System.err.println("Error saving announcement: " + e.getMessage()); }
        }

        Connection getConnection() { return conn; }
        int totalStock() { int t = 0; for (int u : stock.values()) t += u; return t; }
        int openRequests() { int t = 0; for (EmergencyRequest r : requests) if (r.remainingUnits() > 0) t++; return t; }
    }

    // ── Smooth Scroll ─────────────────────────────────────────────────────────
    private static class SmoothScrollHandler implements MouseWheelListener {
        private final JScrollPane pane;
        private final javax.swing.Timer timer;
        private JScrollBar activeBar;
        private double targetValue;
        private long lastFrameTime;

        SmoothScrollHandler(JScrollPane pane) {
            this.pane = pane;
            this.timer = new javax.swing.Timer(ANIMATION_DELAY_MS, e -> step());
        }

        @Override public void mouseWheelMoved(MouseWheelEvent e) {
            activeBar = e.isShiftDown() ? pane.getHorizontalScrollBar() : pane.getVerticalScrollBar();
            if (activeBar == null || !activeBar.isVisible()) return;
            if (!timer.isRunning()) { targetValue = activeBar.getValue(); lastFrameTime = System.nanoTime(); }
            targetValue = clamp(targetValue + e.getPreciseWheelRotation() * 82.0, activeBar.getMinimum(), max(activeBar));
            e.consume();
            if (!timer.isRunning()) timer.start();
        }

        private void step() {
            if (activeBar == null) { timer.stop(); return; }
            long now = System.nanoTime();
            double elapsed = (now - lastFrameTime) / 1_000_000_000.0;
            lastFrameTime = now;
            double cur = activeBar.getValue();
            double next = cur + (targetValue - cur) * (1 - Math.exp(-elapsed * 16.0));
            if (Math.abs(targetValue - next) < 0.8) { activeBar.setValue((int) Math.round(targetValue)); timer.stop(); return; }
            activeBar.setValue((int) Math.round(next));
        }

        private static int max(JScrollBar b) { return Math.max(b.getMinimum(), b.getMaximum() - b.getVisibleAmount()); }
        private static double clamp(double v, double mn, double mx) { return Math.max(mn, Math.min(mx, v)); }
    }

    // ── Combo Renderer ────────────────────────────────────────────────────────
    private static class AnimatedComboRenderer extends JLabel implements ListCellRenderer<Object> {
        AnimatedComboRenderer() { setOpaque(true); setBorder(new EmptyBorder(8, 11, 8, 11)); setFont(getFont().deriveFont(13f)); }
        @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean foc) {
            setText(value == null ? "" : value.toString());
            setForeground(sel ? PRIMARY_DARK : INK);
            setBackground(sel ? SELECTION : index < 0 ? PANEL_BG : Color.WHITE);
            setBorder(new EmptyBorder(8, 11, 8, 11));
            return this;
        }
    }

    // ── Animated ComboBox UI ──────────────────────────────────────────────────
    private static class AnimatedComboBoxUI extends BasicComboBoxUI {
        private javax.swing.Timer animationTimer;
        private PopupMenuListener popupMenuListener;
        private double openProgress, targetProgress;
        private long lastFrameTime;

        @Override protected void installListeners() {
            super.installListeners();
            popupMenuListener = new PopupMenuListener() {
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) { animateTo(1.0); }
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { animateTo(0.0); }
                public void popupMenuCanceled(PopupMenuEvent e) { animateTo(0.0); }
            };
            comboBox.addPopupMenuListener(popupMenuListener);
        }

        @Override protected void uninstallListeners() {
            if (popupMenuListener != null) comboBox.removePopupMenuListener(popupMenuListener);
            if (animationTimer != null) animationTimer.stop();
            super.uninstallListeners();
        }

        @Override protected JButton createArrowButton() {
            JButton btn = new JButton() {
                @Override protected void paintComponent(Graphics g0) {
                    Graphics2D g = (Graphics2D) g0.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int cx = getWidth() / 2, cy = getHeight() / 2;
                    g.rotate(Math.PI * openProgress, cx, cy);
                    g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.setColor(blend(MUTED, PRIMARY, (float) openProgress));
                    g.drawLine(cx - 5, cy - 2, cx, cy + 4); g.drawLine(cx, cy + 4, cx + 5, cy - 2);
                    g.dispose();
                }
            };
            btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false);
            btn.setFocusPainted(false); btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return btn;
        }

        @Override public void paint(Graphics g0, JComponent c) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, c.getHeight(), blend(PANEL_BG, SELECTION, (float) (0.55 * openProgress))));
            g.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 8, 8);
            g.dispose();
            super.paint(g0, c);
            g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(blend(BORDER, ACCENT, (float) openProgress));
            g.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 8, 8);
            if (openProgress > 0.02) { g.setColor(new Color(244, 63, 94, (int) Math.round(44 * openProgress))); g.drawRoundRect(1, 1, c.getWidth() - 3, c.getHeight() - 3, 8, 8); }
            g.dispose();
        }

        @Override public void paintCurrentValueBackground(Graphics g0, Rectangle bounds, boolean hasFocus) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setColor(new Color(255, 255, 255, 0));
            g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
            g.dispose();
        }

        private void animateTo(double target) {
            targetProgress = target;
            if (animationTimer != null && animationTimer.isRunning()) return;
            lastFrameTime = System.nanoTime();
            animationTimer = new javax.swing.Timer(ANIMATION_DELAY_MS, e -> stepAnimation());
            animationTimer.start();
        }

        private void stepAnimation() {
            long now = System.nanoTime();
            double elapsed = (now - lastFrameTime) / 1_000_000_000.0;
            lastFrameTime = now;
            openProgress += (targetProgress - openProgress) * (1 - Math.exp(-elapsed * 18.0));
            if (Math.abs(targetProgress - openProgress) < 0.01) { openProgress = targetProgress; animationTimer.stop(); }
            if (comboBox != null) comboBox.repaint();
            if (arrowButton != null) arrowButton.repaint();
        }
    }

    // ── Nav Button ────────────────────────────────────────────────────────────
    private static class NavButton extends JButton {
        private final String section;
        NavButton(String section, Icon icon) {
            super("", icon); this.section = section;
            setUI(new BasicButtonUI()); setToolTipText(section);
            setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(true); setOpaque(true);
            setForeground(new Color(255, 228, 230)); setBackground(PRIMARY_DARK);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(getFont().deriveFont(Font.BOLD, 13f));
            setHorizontalAlignment(SwingConstants.LEFT); setIconTextGap(14);
            setBorder(new EmptyBorder(10, 12, 10, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setPreferredSize(new Dimension(NAV_COLLAPSED_WIDTH - 16, 44));
            setExpanded(false); addChangeListener(e -> refreshColors());
        }
        String getSection() { return section; }
        void setExpanded(boolean expanded) {
            setText(expanded ? section : "");
            setPreferredSize(new Dimension((expanded ? NAV_EXPANDED_WIDTH : NAV_COLLAPSED_WIDTH) - 16, 44));
            revalidate(); repaint();
        }
        void refreshColors() {
            ButtonModel m = getModel();
            if (isSelected()) { setBackground(PRIMARY); setForeground(Color.WHITE); }
            else if (m.isPressed() || m.isRollover()) { setBackground(new Color(131, 24, 67)); setForeground(Color.WHITE); }
            else { setBackground(PRIMARY_DARK); setForeground(new Color(255, 228, 230)); }
        }
        @Override public void setSelected(boolean sel) { super.setSelected(sel); refreshColors(); }
    }

    // ── Nav Icon ──────────────────────────────────────────────────────────────
    private static class NavIcon implements Icon {
        private static final int SIZE = 22;
        private final int type;
        NavIcon(int type) { this.type = type; }
        @Override public int getIconWidth() { return SIZE; }
        @Override public int getIconHeight() { return SIZE; }
        @Override public void paintIcon(Component c, Graphics g0, int x, int y) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(c.getForeground());
            switch (type) {
                case 0: g.drawRoundRect(x+2,y+2,7,7,2,2); g.drawRoundRect(x+13,y+2,7,7,2,2); g.drawRoundRect(x+2,y+13,7,7,2,2); g.drawRoundRect(x+13,y+13,7,7,2,2); break;
                case 1: g.drawOval(x+7,y+2,8,8); g.drawArc(x+4,y+10,14,10,0,180); g.drawLine(x+11,y+13,x+11,y+19); g.drawLine(x+7,y+16,x+15,y+16); break;
                case 2: Polygon drop = new Polygon(); drop.addPoint(x+11,y+2); drop.addPoint(x+17,y+11); drop.addPoint(x+16,y+17); drop.addPoint(x+11,y+21); drop.addPoint(x+6,y+17); drop.addPoint(x+5,y+11); g.drawPolygon(drop); break;
                case 3: g.drawRoundRect(x+4,y+3,14,16,3,3); g.drawLine(x+11,y+7,x+11,y+15); g.drawLine(x+7,y+11,x+15,y+11); break;
                case 4: g.drawOval(x+2,y+2,18,18); g.drawLine(x+6,y+11,x+10,y+15); g.drawLine(x+10,y+15,x+16,y+7); break;
                case 5: g.drawOval(x+3,y+3,16,16); g.drawLine(x+11,y+11,x+11,y+6); g.drawLine(x+11,y+11,x+15,y+13); break;
                case 6: g.drawLine(x+3,y+19,x+20,y+19); g.fillRoundRect(x+5,y+11,3,7,2,2); g.fillRoundRect(x+10,y+6,3,12,2,2); g.fillRoundRect(x+15,y+9,3,9,2,2); break;
                default: Polygon shield = new Polygon(); shield.addPoint(x+11,y+2); shield.addPoint(x+18,y+5); shield.addPoint(x+17,y+13); shield.addPoint(x+11,y+20); shield.addPoint(x+5,y+13); shield.addPoint(x+4,y+5); g.drawPolygon(shield); g.drawLine(x+8,y+11,x+11,y+14); g.drawLine(x+11,y+14,x+15,y+8); break;
            }
            g.dispose();
        }
    }

    // ── Gradient Panel ────────────────────────────────────────────────────────
    private static class GradientPanel extends JPanel {
        private final Color start, end;
        GradientPanel(LayoutManager layout, Color start, Color end) { super(layout); this.start = start; this.end = end; setOpaque(false); }
        @Override protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, start, getWidth(), getHeight(), end));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(255, 255, 255, 34)); g.fillRect(0, getHeight() - 1, getWidth(), 1);
            g.setColor(new Color(255, 255, 255, 18));
            for (int x = -getHeight(); x < getWidth(); x += 90)
                g.fillPolygon(new int[]{x, x+28, x+getHeight()+28, x+getHeight()}, new int[]{0,0,getHeight(),getHeight()}, 4);
            g.dispose(); super.paintComponent(g0);
        }
    }

    // ── Blood Stock Graph ─────────────────────────────────────────────────────
    private class BloodStockGraphPanel extends JPanel {
        private final Map<String, Double> displayedStock = new LinkedHashMap<>();
        private final Map<String, Integer> targetStock = new LinkedHashMap<>();

        BloodStockGraphPanel() {
            setPreferredSize(new Dimension(100, 250)); setMinimumSize(new Dimension(100, 220));
            setOpaque(false); setBorder(new EmptyBorder(16, 16, 16, 16));
            for (String g : BLOOD_GROUPS) { displayedStock.put(g, 0.0); targetStock.put(g, 0); }
        }

        void setStock(Map<String, Integer> stock) {
            for (String g : BLOOD_GROUPS) targetStock.put(g, stock.getOrDefault(g, 0));
            repaint();
        }

        void stepAnimation(double elapsed) {
            boolean changed = false;
            double s = 1 - Math.exp(-elapsed * 9.5);
            for (String g : BLOOD_GROUPS) {
                double cur = displayedStock.getOrDefault(g, 0.0), tgt = targetStock.getOrDefault(g, 0);
                double nxt = cur + (tgt - cur) * s;
                if (Math.abs(nxt - cur) > 0.02) changed = true;
                displayedStock.put(g, nxt);
            }
            if (changed) repaint();
        }

        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, PANEL_BG, 0, getHeight(), new Color(248, 250, 252)));
            g.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
            g.setColor(BORDER); g.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);

            int left=54, right=28, top=70, bottom=48;
            int gw = Math.max(1, getWidth()-left-right), gh = Math.max(1, getHeight()-top-bottom);
            int maxU = 1; for (int v : targetStock.values()) maxU = Math.max(maxU, v);
            int axMax = Math.max(4, ((maxU+3)/4)*4);

            g.setFont(getFont().deriveFont(Font.BOLD, 17f)); g.setColor(INK);
            g.drawString("Blood Group Wise Quantity", 22, 31);
            g.setFont(getFont().deriveFont(12f)); g.setColor(MUTED);
            g.drawString("Available units by group", 22, 50);

            FontMetrics fm = g.getFontMetrics();
            g.setColor(new Color(226, 232, 240));
            for (int i = 0; i <= 4; i++) {
                int gy = top + Math.round(gh * i / 4f);
                g.drawLine(left, gy, left+gw, gy);
                String lbl = String.valueOf(axMax - axMax*i/4);
                g.setColor(MUTED); g.drawString(lbl, left-fm.stringWidth(lbl)-10, gy+4);
                g.setColor(new Color(226, 232, 240));
            }
            g.setColor(new Color(203, 213, 225));
            g.drawLine(left, top, left, top+gh); g.drawLine(left, top+gh, left+gw, top+gh);

            int gap=16, bc=BLOOD_GROUPS.length;
            int bw = Math.max(24, (gw-gap*(bc-1))/bc);
            if (bw*bc+gap*(bc-1) > gw) { gap=8; bw=Math.max(18,(gw-gap*(bc-1))/bc); }
            int startX = left + Math.max(0, (gw - (bw*bc+gap*(bc-1)))/2);

            for (int i = 0; i < BLOOD_GROUPS.length; i++) {
                String grp = BLOOD_GROUPS[i];
                double units = displayedStock.getOrDefault(grp, 0.0);
                int bh = (int) Math.round((units/axMax)*gh);
                int bx = startX + i*(bw+gap), by = top+gh-bh;
                Color col = BLOOD_COLORS.get(grp);
                g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 24));
                g.fillRoundRect(bx, top, bw, gh, 8, 8);
                if (bh > 0) {
                    g.setPaint(new GradientPaint(bx, by, blend(col, Color.WHITE, 0.20f), bx, top+gh, col.darker()));
                    g.fillRoundRect(bx, by, bw, Math.max(5, bh), 8, 8);
                }
                g.setColor(INK);
                String val = String.valueOf(Math.round(units));
                g.drawString(val, bx+(bw-fm.stringWidth(val))/2, Math.max(top+14, by-8));
                g.setColor(MUTED);
                g.drawString(grp, bx+(bw-fm.stringWidth(grp))/2, top+gh+24);
            }
            g.dispose();
        }
    }

    // ── Announcement Banner ───────────────────────────────────────────────────
    private static class AnnouncementBanner extends JPanel {
        private String announcement = "No emergency announcement";
        private int textX;
        AnnouncementBanner() {
            setOpaque(true); setBackground(new Color(88, 12, 43)); setForeground(Color.YELLOW);
            setFont(getFont().deriveFont(Font.BOLD, 16f)); setBorder(new EmptyBorder(6, 18, 6, 18));
            setPreferredSize(new Dimension(100, 32));
        }
        void setAnnouncement(String ann, int x) { this.announcement = ann; this.textX = x; repaint(); }
        @Override protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(getForeground()); g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            g.drawString(announcement, textX, (getHeight()-fm.getHeight())/2+fm.getAscent());
            g.dispose();
        }
    }

    // ── Data Model Classes ────────────────────────────────────────────────────
    private static class Donor {
        final int id; final String name, phone, gender, bloodGroup, area, city;
        final int age; final boolean available;
        Donor(int id, String name, String phone, int age, String gender, String bloodGroup, String area, String city, boolean available) {
            this.id=id; this.name=name; this.phone=phone; this.age=age; this.gender=gender;
            this.bloodGroup=bloodGroup; this.area=area; this.city=city; this.available=available;
        }
    }

    private static class EmergencyRequest {
        final int id; final String patientName, bloodGroup, hospital, area;
        final int neededUnits; int distributedUnits;
        String priority, status = "Pending";
        EmergencyRequest(int id, String patientName, String bloodGroup, int neededUnits, String hospital, String area, String priority) {
            this.id=id; this.patientName=patientName; this.bloodGroup=bloodGroup;
            this.neededUnits=neededUnits; this.hospital=hospital; this.area=area; this.priority=priority;
        }
        int remainingUnits() { return neededUnits - distributedUnits; }
    }

    private static class Transaction {
        final String time, type, bloodGroup, hospitalOrSource, area, notes;
        final int units;
        int donorId = -1;
        int requestId = -1;
        Transaction(String type, String bloodGroup, int units, String hospitalOrSource, String area, String notes) {
            this.time = LocalDateTime.now().format(DATE_FORMAT);
            this.type=type; this.bloodGroup=bloodGroup; this.units=units;
            this.hospitalOrSource=hospitalOrSource; this.area=area; this.notes=notes;
        }
        // Constructor used when loading from DB (preserve original timestamp)
        Transaction(String time, String type, String bloodGroup, int units, String hospitalOrSource, String area, String notes) {
            this.time = time == null ? LocalDateTime.now().format(DATE_FORMAT) : time;
            this.type = type; this.bloodGroup = bloodGroup; this.units = units;
            this.hospitalOrSource = hospitalOrSource; this.area = area; this.notes = notes;
        }
        Transaction setDonor(int donorId) { this.donorId = donorId; return this; }
        Transaction setRequest(int requestId) { this.requestId = requestId; return this; }
        static Transaction stockIn(String bg, int units, String src, String area) { return new Transaction("Stock In", bg, units, src, area, "Blood stock received"); }
        static Transaction stockIn(String bg, int units, String src, String area, String notes) { return new Transaction("Stock In", bg, units, src, area, notes); }
        static Transaction distribution(String bg, int units, String hospital, String area, String notes) { return new Transaction("Distribution", bg, units, hospital, area, notes); }
        static Transaction admin(String bg, int units, String src, String area, String notes) { return new Transaction("Admin", bg, units, src, area, notes); }
    }
}
