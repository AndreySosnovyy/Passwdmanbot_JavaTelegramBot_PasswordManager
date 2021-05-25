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

    // ============================================================================================================= //

    // ��������� �� ����
    public static final String USE_MENU = SCROLL_EMOJI + " | �������������� ��������������� ����:";
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

    // ============================================================================================================= //

    // ������
    public static final String VIEW_REPOSITORY = OPEN_FILE_FOLDER_EMOJI + " ���������";
    public static final String GENERATE_PASSWORD = LOCK_WITH_INK_PEN_EMOJI + " ������������� ������";
    public static final String SETTINGS = GEAR_EMOJI + " ���������";
    public static final String BACK = BACK_EMOJI + " �����";
    public static final String ADD_NEW_PASSWORD = PLUS_EMOJI + " �������� ����� ������";
}
