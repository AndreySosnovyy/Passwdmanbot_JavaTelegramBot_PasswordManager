package ru.andreysosnovyy.config;

public class Messages {

    // emoji https://emojipedia.org/
    private static final String CHECK_MARK_EMOJI = Character.toString(0x2705);
    private static final String CHECK_EMOJI = Character.toString(0x2714);
    private static final String CROSS_MARK_EMOJI = Character.toString(0x274C);
    private static final String EXCLAMATION_MARK_EMOJI = Character.toString(0x2757);
    private static final String NO_ENTRY_SIGN_EMOJI = Character.toString(0x1F6AB);
    private static final String SQUARED_OK_EMOJI = Character.toString(0x1F197);
    private static final String NO_ENTRY_EMOJI = Character.toString(0x26D4);
    private static final String HEAVY_LARGE_CIRCLE_EMOJI = Character.toString(0x2B55);
    private static final String STAR_EMOJI = Character.toString(0x2B50);
    private static final String PARTY_POPPER_EMOJI = Character.toString(0x1F389);
    private static final String FLOPPY_DISK_EMOJI = Character.toString(0x1F4BE);
    private static final String FILE_FOLDER_EMOJI = Character.toString(0x1F4C1);
    private static final String OPEN_FILE_FOLDER_EMOJI = Character.toString(0x1F4C2);
    private static final String LOCK_WITH_INK_PEN_EMOJI = Character.toString(0x1F50F);
    private static final String CLOSED_LOCK_WITH_KEY_EMOJI = Character.toString(0x1F510);
    private static final String KEY_EMOJI = Character.toString(0x1F511);
    private static final String LOCK_EMOJI = Character.toString(0x1F512);
    private static final String OPEN_LOCK_EMOJI = Character.toString(0x1F513);
    private static final String PLUS_EMOJI = Character.toString(0x2795);
    private static final String MINUS_EMOJI = Character.toString(0x2796);
    private static final String GEAR_EMOJI = Character.toString(0x2699);
    private static final String BACK_EMOJI = Character.toString(0x21A9);
    private static final String SCROLL_EMOJI = Character.toString(0x1F4DC);
    private static final String SCISSORS_EMOJI = Character.toString(0x2702);
    private static final String MAGNIFIER_EMOJI = Character.toString(0x1F50E);
    private static final String BOOKMARK_TABS_EMOJI = Character.toString(0x1F4D1);
    private static final String WARNING_EMOJI = Character.toString(0x26A0);
    private static final String KEYBOARD_EMOJI = Character.toString(0x2328);
    private static final String DOOR_EMOJI = Character.toString(0x1F6AA);
    private static final String PENCIL_EMOJI = Character.toString(0x270F);
    private static final String QUESTION_EMOJI = Character.toString(0x2753);
    private static final String WAVING_HAND_EMOJI = Character.toString(0x1F44B);
    private static final String MAN_SHRUGGING_EMOJI = Character.toString(0x1F937);

    public static final String LEFT_ARROW = Character.toString(0x276E);
    public static final String RIGHT_ARROW = Character.toString(0x276F);
    public static final String DOT = Character.toString(0x2022);

    // ============================================================================================================= //

    // ��������� �� ����
    public static final String USE_MENU = BOOKMARK_TABS_EMOJI + " | �������������� ��������������� ����:";
    public static final String TAP_TO_CHOOSE = SCISSORS_EMOJI + " | �������� ���� �������, ����� ���������� ��� " +
            "(�� �������� ������������� ����� 15 ������)";
    public static final String CREATE_REPOSITORY_PASSWORD = EXCLAMATION_MARK_EMOJI +
            " | ��� ����, ����� ������ ������������ ������������ ���������, " +
            "���������� ������� ������ ������-������, ����� ������ �� ����� ����� � ���� ������. ���������� " +
            "�������� ������ � ��������� ���:";
    public static final String CONFIRM_REPOSITORY_PASSWORD = CHECK_EMOJI + " | ����������� ��������� ������ (� ��� ������):";
    public static final String TRY_AGAIN_REPOSITORY_PASSWORD = CROSS_MARK_EMOJI +
            " | ������ �� ���������, ���������� ��� ��� (����� ������ �� ������� ��������� ����� ������-������):";
    public static final String CREATED_SUCCESSFUL = PARTY_POPPER_EMOJI + " | ������-������ ������� ������!";
    public static final String ENTER_REPO_PASS = KEY_EMOJI + " | ������� ���� ������-������:";
    public static final String WRONG_REPO_PASS = NO_ENTRY_EMOJI + " | �������� ������!";
    public static final String USE_REPO_MENU = BOOKMARK_TABS_EMOJI + " | �������������� ������������� ���� �� ���������:";
    public static final String ENTER_SERVICE_NAME = KEYBOARD_EMOJI + " | ������� �������� �������:";
    public static final String SERVICE_NAME_EXISTS = CROSS_MARK_EMOJI + " | ������ �������� ������� ��� ������!";
    public static final String ENTER_LOGIN = KEYBOARD_EMOJI + " | ������� �����:";
    public static final String ENTER_PASSWORD = KEYBOARD_EMOJI + " | ������� ������:";
    public static final String ENTER_COMMENT = KEYBOARD_EMOJI + " | ������� ����������� ('-' ���� �� ���������):";
    public static final String SESSION_NOT_ACTIVE = WARNING_EMOJI + " | ������ �� �������!";
    public static final String TIME_RAN_OUT = WARNING_EMOJI + " | ����� �� ���������� �����!";
    public static final String RECORD_SUCCESSFULLY_ADDED = CHECK_EMOJI + " | ������ ������� ���������!";
    public static final String ENTER_SEARCH_STRING = KEYBOARD_EMOJI + " | ������� ��������� ������:";
    public static final String WATCHING_RECORD_INSTRUCTION = FLOPPY_DISK_EMOJI + " �������� ������� | ����� | ������ | ����������� ";
    public static final String RECORD_NOT_ADDED = CROSS_MARK_EMOJI + " | �� ������� �������� ������! ���������� ��� ���...";
    public static final String CONFIRM_DELETE_REPO = QUESTION_EMOJI + " | �� �������, ��� ������ ������� ��������� (��� ���� ������)?";
    public static final String ENTER_NEW_REPO_PASS = KEY_EMOJI + " | ������� ����� ������-������ ��� ������ ���������:";
    public static final String REPO_PASS_CHANGED = CHECK_EMOJI + " | ��� ������-������ ������� �������";
    public static final String SEE_YOU = WAVING_HAND_EMOJI + " | ���� ��������� �������. �������, �������� �����!";
    public static final String CONTACT_ADMIN = MAN_SHRUGGING_EMOJI + " | � ���������, ���� ������ ������������ ������-������ " +
            "� �������������� ������. ����� ������� ��� � ������ ������, �������� �������������� t.me/andreysosnovyy. " +
            "�������� ��������� �� ����������. �� ��� �������� ��� �������������� ������� �������������� ������-������";

    // ============================================================================================================= //

    // ������
    public static final String VIEW_REPOSITORY = OPEN_FILE_FOLDER_EMOJI + " ���������";
    public static final String GENERATE_PASSWORD = LOCK_WITH_INK_PEN_EMOJI + " ������������� ������";
    public static final String SETTINGS = GEAR_EMOJI + " ���������";
    public static final String BACK = BACK_EMOJI + " �����";
    public static final String EXIT_REPO = DOOR_EMOJI + " ����� �� ���������";
    public static final String ADD_NEW_PASSWORD = PLUS_EMOJI + " ��������";
    public static final String SEARCH = MAGNIFIER_EMOJI + " �����";
    public static final String DELETE_PASSWORD = MINUS_EMOJI + " ������� ������";
    public static final String CANCEL = CROSS_MARK_EMOJI + " ������";
    public static final String CHANGE_MASTER_PASS = PENCIL_EMOJI + " �������� ������-������";
    public static final String RESTORE_MASTER_PASS = KEY_EMOJI + " ������������ ������-������";
    public static final String DELETE_REPO = CROSS_MARK_EMOJI + " ������� ���������";
    public static final String YES = "��";
    public static final String EXIT_SETTINGS = DOOR_EMOJI + " �������� ���������";
    public static final String EDIT_RECORD_PASSWORD = PENCIL_EMOJI + " ������������� ������";
    public static final String EDIT_RECORD_COMMENT = PENCIL_EMOJI + " ������������� �����������";
    public static final String DELETE_RECORD = CROSS_MARK_EMOJI + " ������� ��� ������";
}
