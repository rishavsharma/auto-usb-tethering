package com.matkosmoljan.apps.autousbtethering.domain.shell

import android.os.Build
import android.os.Build.VERSION_CODES.*
import android.preference.PreferenceManager
import com.matkosmoljan.apps.autousbtethering.FunctionResult
import com.matkosmoljan.apps.autousbtethering.domain.TetherSwitch
import java.io.DataOutputStream


/**
 * A [TetherSwitch] based on shell command execution
 */
class ShellTetherSwitch : TetherSwitch {

    private val androidApiVersionToMethodNumbers = mapOf(
        KITKAT to 34,
        LOLLIPOP to 30,
        N to 33,
        O to 34, //34 doesn't works for Samsung S7
        P to 33
    )

    override fun turnTetheringOn(): FunctionResult<Unit> {
        val methodNumber = getMethodNumber()
        val tetheringShellCommand = TetherSwitchShellCommandGenerator.generateSwitchOnCommand(methodNumber)
        val isCommandExecutedSuccessfully = executeCommand(tetheringShellCommand)

        return if (isCommandExecutedSuccessfully) {
            FunctionResult.Success(Unit)
        } else {
            FunctionResult.Failure(IllegalStateException("The tethering shell call hasn't returned successfully"))
        }
    }

    override fun turnTetheringOff(): FunctionResult<Unit> {
        val methodNumber = getMethodNumber()
        val tetheringShellCommand = TetherSwitchShellCommandGenerator.generateSwitchOffCommand(methodNumber)
        val isCommandExecutedSuccessfully = executeCommand(tetheringShellCommand)

        return if (isCommandExecutedSuccessfully) {
            FunctionResult.Success(Unit)
        } else {
            FunctionResult.Failure(IllegalStateException("The tethering shell call hasn't returned successfully"))
        }
    }

    override fun turnTetheringOn(method:String): FunctionResult<Unit> {
        val tetheringShellCommand = TetherSwitchShellCommandGenerator.generateSwitchOnCommand(method)
        val isCommandExecutedSuccessfully = executeCommand(tetheringShellCommand)

        return if (isCommandExecutedSuccessfully) {
            FunctionResult.Success(Unit)
        } else {
            FunctionResult.Failure(IllegalStateException("The tethering shell call hasn't returned successfully"))
        }
    }

    override fun turnTetheringOff(method:String): FunctionResult<Unit> {
        val tetheringShellCommand = TetherSwitchShellCommandGenerator.generateSwitchOffCommand(method)
        val isCommandExecutedSuccessfully = executeCommand(tetheringShellCommand)

        return if (isCommandExecutedSuccessfully) {
            FunctionResult.Success(Unit)
        } else {
            FunctionResult.Failure(IllegalStateException("The tethering shell call hasn't returned successfully"))
        }
    }

    override fun getMethodNumber(): Int {

        val currentApiVersion = Build.VERSION.SDK_INT

        return androidApiVersionToMethodNumbers
            .filterKeys { api -> api <= currentApiVersion }
            .maxBy { entry -> entry.key }
            ?.value
            ?: throw NullPointerException("A method number hasn't been found. This should be impossible.")
    }

    /**
     * @return true if the command executed successfully, otherwise false
     */
    // Reference: https://stackoverflow.com/a/3055786
    @Throws(Exception::class)
    private fun executeCommand(command: String): Boolean {
        val process = Runtime.getRuntime().exec("su")
        val os = DataOutputStream(process.outputStream)

        os.writeBytes(command + "\n")
        os.writeBytes("exit\n")
        os.flush()
        os.close()

        return process.waitFor() == 0
    }
}