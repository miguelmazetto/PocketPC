/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.offsec.nhterm.util;

import com.offsec.nhterm.R;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.KeyEvent;

/**
 * Terminal emulator settings
 */
public class TermSettings {
    private SharedPreferences mPrefs;

    private int mStatusBar;
    private boolean mFunctionBar;
    private int mActionBarMode;
    private int mOrientation;
    private int mCursorStyle;
    private int mCursorBlink;
    private int mFontSize;
    private int mFontLeading;
    private String mFontFile;
    private int mAmbiWidth;
    private boolean mHwAcceleration;
    private int mColorId;
    private int mIMEColor;
    private boolean mUTF8ByDefault;
    private int mActionBarIconAction;
    private int mActionBarPlusAction;
    private int mActionBarMinusAction;
    private int mActionBarXAction;
    private int mActionBarUserAction;
    private int mBackKeyAction;
    private int mControlKeyId;
    private int mFnKeyId;
    private int mUseCookedIME;
    private String mShell;
    private String mFailsafeShell;
    private String mInitialCommand;
    private String mIntentCommand;
    private String mTermType;
    private boolean mCloseOnExit;
    private boolean mVerifyPath;
    private boolean mDoPathExtensions;
    private boolean mAllowPathPrepend;
    private String mHomePath;

    private String mPrependPath = null;
    private String mAppendPath = null;

    private boolean mAltSendsEsc;

    private boolean mMouseTracking;

    private boolean mUseKeyboardShortcuts;

    private static final String STATUSBAR_KEY = "statusbar";
    private static final String FUNCTIONBAR_KEY = "functionbar";
    private static final String ACTIONBAR_KEY = "actionbar";
    private static final String ORIENTATION_KEY = "orientation";
    private static final String FONTSIZE_KEY = "fontsize";
    private static final String FONTLEADING_KEY = "fontleading";
    private static final String FONTFILE_KEY = "fontfile";
    private static final String AMBIWIDTH_KEY = "ambiwidth";
    private static final String COLOR_KEY = "color";
    private static final String IMECOLOR_KEY = "composingtext";
    private static final String UTF8_KEY = "utf8_by_default";
    private static final String HWACCELERATION_KEY = "hw_acceleration_by_default";
    private static final String ACTIONBAR_ICON_KEY = "actionbar_icon_action";
    private static final String ACTIONBAR_PLUS_KEY = "actionbar_plus_action";
    private static final String ACTIONBAR_MINUS_KEY = "actionbar_minus_action";
    private static final String ACTIONBAR_X_KEY    = "actionbar_x_action";
    private static final String ACTIONBAR_USER_KEY = "actionbar_user_action";
    private static final String BACKACTION_KEY = "backaction";
    private static final String CONTROLKEY_KEY = "controlkey";
    private static final String FNKEY_KEY = "fnkey";
    private static final String IME_KEY = "ime";
    private static final String SHELL_KEY = "shell";
    private static final String INITIALCOMMAND_KEY = "initialcommand";
    private static final String INTENTCOMMAND_KEY = "intentcommand";
    private static final String TERMTYPE_KEY = "termtype";
    private static final String CLOSEONEXIT_KEY = "close_window_on_process_exit";
    private static final String VERIFYPATH_KEY = "verify_path";
    private static final String PATHEXTENSIONS_KEY = "do_path_extensions";
    private static final String PATHPREPEND_KEY = "allow_prepend_path";
    private static final String HOMEPATH_KEY = "home_path";
    private static final String ALT_SENDS_ESC = "alt_sends_esc";
    private static final String MOUSE_TRACKING = "mouse_tracking";
    private static final String USE_KEYBOARD_SHORTCUTS = "use_keyboard_shortcuts";

    public static final int WHITE               = 0xffffffff;
    public static final int BLACK               = 0xff000000;
    public static final int BLUE                = 0xff344ebd;
    public static final int GREEN               = 0xff00ff00;
    public static final int AMBER               = 0xffffb651;
    public static final int RED                 = 0xffff0113;
    public static final int HOLO_BLUE           = 0xff33b5e5;
    public static final int SOLARIZED_FG        = 0xff657b83;
    public static final int SOLARIZED_BG        = 0xfffdf6e3;
    public static final int SOLARIZED_DARK_FG   = 0xff839496;
    public static final int SOLARIZED_DARK_BG   = 0xff002b36;
    public static final int LINUX_CONSOLE_WHITE = 0xffaaaaaa;

    // foreground color, background color
    public static final int[][] COLOR_SCHEMES = {
            {BLACK,             WHITE},
            {WHITE,             BLACK},
            {WHITE,             BLUE},
            {GREEN,             BLACK},
            {AMBER,             BLACK},
            {RED,               BLACK},
            {HOLO_BLUE,         BLACK},
            {SOLARIZED_FG,      SOLARIZED_BG},
            {SOLARIZED_DARK_FG, SOLARIZED_DARK_BG},
            {LINUX_CONSOLE_WHITE, BLACK}
    };

    public static final int ACTION_BAR_MODE_NONE = 0;
    public static final int ACTION_BAR_MODE_ALWAYS_VISIBLE = 1;
    public static final int ACTION_BAR_MODE_HIDES = 2;
    private static final int ACTION_BAR_MODE_MAX = 3;

    public static final int ORIENTATION_UNSPECIFIED = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;
    public static final int ORIENTATION_PORTRAIT = 2;

    /** An integer not in the range of real key codes. */
    public static final int KEYCODE_NONE = -1;

    public static final int CONTROL_KEY_ID_NONE = 7;
    public static final int[] CONTROL_KEY_SCHEMES = {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_AT,
            KeyEvent.KEYCODE_ALT_LEFT,
            KeyEvent.KEYCODE_ALT_RIGHT,
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_CAMERA,
            KEYCODE_NONE
    };

    public static final int FN_KEY_ID_NONE = 7;
    public static final int[] FN_KEY_SCHEMES = {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_AT,
            KeyEvent.KEYCODE_ALT_LEFT,
            KeyEvent.KEYCODE_ALT_RIGHT,
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_CAMERA,
            KEYCODE_NONE
    };

    public static final int BACK_KEY_STOPS_SERVICE = 0;
    public static final int BACK_KEY_CLOSES_WINDOW = 1;
    public static final int BACK_KEY_CLOSES_ACTIVITY = 2;
    public static final int BACK_KEY_SENDS_ESC = 3;
    public static final int BACK_KEY_SENDS_TAB = 4;
    public static final int BACK_KEY_TOGGLE_IME = 5;
    private static final int BACK_KEY_MAX = 5;
    private static final int ACTIONBAR_KEY_MAX = 65535;

    public TermSettings(Resources res, SharedPreferences prefs) {
        readDefaultPrefs(res);
        readPrefs(prefs);
    }

    private void readDefaultPrefs(Resources res) {
        mStatusBar = Integer.parseInt(res.getString(R.string.pref_statusbar_default));
        mFunctionBar = res.getBoolean(R.bool.pref_functionbar_default);
        mActionBarMode = res.getInteger(R.integer.pref_actionbar_default);
        mOrientation = res.getInteger(R.integer.pref_orientation_default);
        mCursorStyle = Integer.parseInt(res.getString(R.string.pref_cursorstyle_default));
        mCursorBlink = Integer.parseInt(res.getString(R.string.pref_cursorblink_default));
        mFontSize = Integer.parseInt(res.getString(R.string.pref_fontsize_default));
        mFontLeading = Integer.parseInt(res.getString(R.string.pref_fontleading_default));
        mFontFile = res.getString(R.string.pref_fontfile_default);
        mAmbiWidth = Integer.parseInt(res.getString(R.string.pref_ambiwidth_default));
        mColorId = Integer.parseInt(res.getString(R.string.pref_color_default));
        mIMEColor = Integer.parseInt(res.getString(R.string.pref_composingtext_default));
        mUTF8ByDefault = res.getBoolean(R.bool.pref_utf8_by_default_default);
        mHwAcceleration = res.getBoolean(R.bool.pref_hw_acceleration_by_default);
        mActionBarIconAction = Integer.parseInt(res.getString(R.string.pref_actionbar_icon_default));
        mActionBarPlusAction = Integer.parseInt(res.getString(R.string.pref_actionbar_plus_default));
        mActionBarMinusAction = Integer.parseInt(res.getString(R.string.pref_actionbar_minus_default));
        mActionBarXAction = Integer.parseInt(res.getString(R.string.pref_actionbar_x_default));
        mActionBarUserAction = Integer.parseInt(res.getString(R.string.pref_actionbar_user_default));
        mBackKeyAction = Integer.parseInt(res.getString(R.string.pref_backaction_default));
        mControlKeyId = Integer.parseInt(res.getString(R.string.pref_controlkey_default));
        mFnKeyId = Integer.parseInt(res.getString(R.string.pref_fnkey_default));
        mUseCookedIME = Integer.parseInt(res.getString(R.string.pref_ime_default));
        mFailsafeShell = res.getString(R.string.pref_shell_default);
        mShell = mFailsafeShell;
        mInitialCommand = res.getString(R.string.pref_initialcommand_default);
        mIntentCommand = res.getString(R.string.pref_intentcommand_default);
        mTermType = res.getString(R.string.pref_termtype_default);
        mCloseOnExit = res.getBoolean(R.bool.pref_close_window_on_process_exit_default);
        mVerifyPath = res.getBoolean(R.bool.pref_verify_path_default);
        mDoPathExtensions = res.getBoolean(R.bool.pref_do_path_extensions_default);
        mAllowPathPrepend = res.getBoolean(R.bool.pref_allow_prepend_path_default);
        // the mHomePath default is set dynamically in readPrefs()
        mAltSendsEsc = res.getBoolean(R.bool.pref_alt_sends_esc_default);
        mMouseTracking = res.getBoolean(R.bool.pref_mouse_tracking_default);
        mUseKeyboardShortcuts = res.getBoolean(R.bool.pref_use_keyboard_shortcuts_default);
    }

    public void readPrefs(SharedPreferences prefs) {
        mPrefs = prefs;
        mStatusBar = readIntPref(STATUSBAR_KEY, mStatusBar, 1);
        mFunctionBar = readBooleanPref(FUNCTIONBAR_KEY, mFunctionBar);
        mActionBarMode = readIntPref(ACTIONBAR_KEY, mActionBarMode, ACTION_BAR_MODE_MAX);
        mOrientation = readIntPref(ORIENTATION_KEY, mOrientation, 2);
        // mCursorStyle = readIntPref(CURSORSTYLE_KEY, mCursorStyle, 2);
        // mCursorBlink = readIntPref(CURSORBLINK_KEY, mCursorBlink, 1);
        mFontSize = readIntPref(FONTSIZE_KEY, mFontSize, 288);
        mFontLeading = readIntPref(FONTLEADING_KEY, mFontLeading, 288);
        mFontFile = readStringPref(FONTFILE_KEY, mFontFile);
        mAmbiWidth = readIntPref(AMBIWIDTH_KEY, mAmbiWidth, 3);
        mColorId = readIntPref(COLOR_KEY, mColorId, COLOR_SCHEMES.length - 1);
        mIMEColor = readIntPref(IMECOLOR_KEY, mIMEColor, 100);
        mUTF8ByDefault = readBooleanPref(UTF8_KEY, mUTF8ByDefault);
        mHwAcceleration = readBooleanPref(HWACCELERATION_KEY, mHwAcceleration);
        mActionBarIconAction = readIntPref(ACTIONBAR_ICON_KEY, mActionBarIconAction, ACTIONBAR_KEY_MAX);
        mActionBarPlusAction = readIntPref(ACTIONBAR_PLUS_KEY, mActionBarPlusAction, ACTIONBAR_KEY_MAX);
        mActionBarMinusAction = readIntPref(ACTIONBAR_MINUS_KEY, mActionBarMinusAction, ACTIONBAR_KEY_MAX);
        mActionBarXAction    = readIntPref(ACTIONBAR_X_KEY,    mActionBarXAction,    ACTIONBAR_KEY_MAX);
        mActionBarUserAction = readIntPref(ACTIONBAR_USER_KEY, mActionBarUserAction, ACTIONBAR_KEY_MAX);
        mBackKeyAction = readIntPref(BACKACTION_KEY, mBackKeyAction, BACK_KEY_MAX);
        mControlKeyId = readIntPref(CONTROLKEY_KEY, mControlKeyId,
                CONTROL_KEY_SCHEMES.length - 1);
        mFnKeyId = readIntPref(FNKEY_KEY, mFnKeyId,
                FN_KEY_SCHEMES.length - 1);
        mUseCookedIME = readIntPref(IME_KEY, mUseCookedIME, 1);
        mShell = readStringPref(SHELL_KEY, mShell);
        mInitialCommand = readStringPref(INITIALCOMMAND_KEY, mInitialCommand);
        mIntentCommand = readStringPref(INTENTCOMMAND_KEY, mIntentCommand);
        mTermType = readStringPref(TERMTYPE_KEY, mTermType);
        mCloseOnExit = readBooleanPref(CLOSEONEXIT_KEY, mCloseOnExit);
        mVerifyPath = readBooleanPref(VERIFYPATH_KEY, mVerifyPath);
        mDoPathExtensions = readBooleanPref(PATHEXTENSIONS_KEY, mDoPathExtensions);
        mAllowPathPrepend = readBooleanPref(PATHPREPEND_KEY, mAllowPathPrepend);
        mHomePath = readStringPref(HOMEPATH_KEY, mHomePath);
        mAltSendsEsc = readBooleanPref(ALT_SENDS_ESC, mAltSendsEsc);
        mMouseTracking = readBooleanPref(MOUSE_TRACKING, mMouseTracking);
        mUseKeyboardShortcuts = readBooleanPref(USE_KEYBOARD_SHORTCUTS,
                mUseKeyboardShortcuts);
        mPrefs = null;  // we leak a Context if we hold on to this
    }

    private int readIntPref(String key, int defaultValue, int maxValue) {
        int val;
        try {
            val = Integer.parseInt(
                    mPrefs.getString(key, Integer.toString(defaultValue)));
        } catch (NumberFormatException e) {
            val = defaultValue;
        }
        val = Math.max(0, Math.min(val, maxValue));
        return val;
    }

    private String readStringPref(String key, String defaultValue) {
        return mPrefs.getString(key, defaultValue);
    }

    private boolean readBooleanPref(String key, boolean defaultValue) {
        return mPrefs.getBoolean(key, defaultValue);
    }

    public boolean showStatusBar() {
        return true;
    }

    public boolean showFunctionBar() {
        return mFunctionBar;
    }

    public int actionBarMode() {
        return mActionBarMode;
    }

    public int getScreenOrientation() {
        return mOrientation;
    }

    public int getCursorStyle() {
        return mCursorStyle;
    }

    public int getCursorBlink() {
        return mCursorBlink;
    }

    public int getFontSize() {
        return mFontSize;
    }

    public int getFontLeading() {
        return mFontLeading;
    }

    public String getFontFile() {
        return mFontFile;
    }

    public int getAmbiWidth() {
        return mAmbiWidth;
    }

    public boolean getHwAcceleration() {
        return mHwAcceleration;
    }

    public int[] getColorScheme() {
        return COLOR_SCHEMES[mColorId];
    }

    public int getColorTheme() {
        int bg = COLOR_SCHEMES[mColorId][1];
        return (bg == WHITE || bg == SOLARIZED_BG) ? 1 : 0;
    }

    public int getIMEColor() {
        return mIMEColor;
    }

    public boolean defaultToUTF8Mode() {
        return mUTF8ByDefault;
    }

    public int getActionBarIconKeyAction() {
        return mActionBarIconAction;
    }

    public int getActionBarPlusKeyAction() {
        mActionBarPlusAction = 1250;
        return mActionBarPlusAction; }

    public int getActionBarMinusKeyAction() {
        mActionBarMinusAction = 999;
        return mActionBarMinusAction;
    }

    public int getActionBarXKeyAction() {
        return 1251;
//        return mActionBarXAction;
    }

    public int getActionBarUserKeyAction() {
        mActionBarUserAction = 999;
        return mActionBarUserAction;
    }

    public int getActionBarQuitKeyAction() {
        return mActionBarXAction;
    }

    public int getBackKeyAction() {
        return mBackKeyAction;
    }

    public boolean backKeySendsCharacter() {
        return mBackKeyAction != BACK_KEY_TOGGLE_IME && mBackKeyAction >= BACK_KEY_SENDS_ESC;
    }

    public boolean getAltSendsEscFlag() {
        return mAltSendsEsc;
    }

    public boolean getMouseTrackingFlag() {
        return mMouseTracking;
    }

    public boolean getUseKeyboardShortcutsFlag() {
        return mUseKeyboardShortcuts;
    }

    public int getBackKeyCharacter() {
        switch (mBackKeyAction) {
            case BACK_KEY_SENDS_ESC: return 27;
            case BACK_KEY_SENDS_TAB: return 9;
            default: return 0;
        }
    }

    public int getControlKeyId() {
        return mControlKeyId;
    }

    public int getFnKeyId() {
        return mFnKeyId;
    }

    public int getControlKeyCode() {
        return CONTROL_KEY_SCHEMES[mControlKeyId];
    }

    public int getFnKeyCode() {
        return FN_KEY_SCHEMES[mFnKeyId];
    }

    public boolean useCookedIME() {
        return (mUseCookedIME != 0);
    }

    public String getShell() {
        return mShell;
    }

    public String getFailsafeShell() {
        return mFailsafeShell;
    }

    public String getInitialCommand() {
        return mInitialCommand;
    }

    public String getIntentCommand() {
        return mIntentCommand;
    }

    public String getTermType() {
        return mTermType;
    }

    public boolean closeWindowOnProcessExit() {
        return mCloseOnExit;
    }

    public boolean verifyPath() {
        return mVerifyPath;
    }

    public boolean doPathExtensions() {
        return mDoPathExtensions;
    }

    public boolean allowPathPrepend() {
        return mAllowPathPrepend;
    }

    public void setPrependPath(String prependPath) {
        mPrependPath = prependPath;
    }

    public String getPrependPath() {
        return mPrependPath;
    }

    public void setAppendPath(String appendPath) {
        mAppendPath = appendPath;
    }

    public String getAppendPath() {
        return mAppendPath;
    }

    public void setHomePath(String homePath) {
        mHomePath = homePath;
    }

    public String getHomePath() {
        return mHomePath;
    }
}