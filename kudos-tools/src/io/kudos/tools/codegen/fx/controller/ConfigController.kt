package io.kudos.tools.codegen.fx.controller

import io.kudos.ability.data.rdb.flyway.kit.FlywayKit
import io.kudos.ability.data.rdb.jdbc.kit.DataSourceKit
import io.kudos.ability.data.rdb.jdbc.kit.RdbKit
import io.kudos.base.io.FilenameKit
import io.kudos.base.io.PathKit
import io.kudos.base.lang.SystemKit
import io.kudos.context.core.KudosContext
import io.kudos.context.core.KudosContextHolder
import io.kudos.tools.codegen.model.vo.Config
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.stage.DirectoryChooser
import org.soul.base.support.PropertiesLoader
import org.springframework.boot.autoconfigure.flyway.FlywayProperties
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.*
import javax.sql.DataSource

/**
 * 配置信息界面JavaFx控制器
 *
 * @author K
 * @since 1.0.0
 */
class ConfigController : Initializable {

    @FXML
    lateinit var urlTextField: TextField

    @FXML
    lateinit var userTextField: TextField

    @FXML
    lateinit var passwordField: PasswordField

    @FXML
    lateinit var templateChoiceBox: ComboBox<Config.TemplateNameAndRootDir>

    @FXML
    lateinit var packagePrefixTextField: TextField

    @FXML
    lateinit var moduleTextField: TextField

    @FXML
    lateinit var locationTextField: TextField

    @FXML
    lateinit var openButton: Button

    @FXML
    lateinit var authorTextField: TextField

    @FXML
    lateinit var versionTextField: TextField

    val config = Config()
    private val userHome = System.getProperty("user.home")
    private val propertiesFile = File("$userHome/.kudos/CodeGenerator.properties")
    private lateinit var propertiesLoader: PropertiesLoader
    private var moduleSuggestions: Set<String?>? = null

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        initConfig()
        bindProperties()
        initTempleComboBox()
        initAutoCompletion()
    }

    private fun initAutoCompletion() {
        var moduleSuggestionsStr = propertiesLoader.getProperty(Config.PROP_KEY_MODULE_SUGGESTIONS, "")
        moduleSuggestions = HashSet(listOf(*moduleSuggestionsStr.split(",".toRegex()).toTypedArray()))
    }

    private fun initConfig() {
        val properties = properties
        propertiesLoader = PropertiesLoader(properties)
        with(config) {
            setDbUrl(propertiesLoader.getProperty(Config.PROP_KEY_DB_URL, ""))
            setDbUser(propertiesLoader.getProperty(Config.PROP_KEY_DB_USER, ""))
            setDbPassword(propertiesLoader.getProperty(Config.PROP_KEY_DB_PASSWORD, ""))
            val templateInfo = Config.TemplateNameAndRootDir(
                propertiesLoader.getProperty(Config.PROP_KEY_TEMPLATE_ROOT_DIR, ""),
                FilenameKit.normalize(propertiesLoader.getProperty(Config.PROP_KEY_TEMPLATE_ROOT_DIR, ""), true)
            )
            setTemplateInfo(templateInfo)
            setPackagePrefix(propertiesLoader.getProperty(Config.PROP_KEY_PACKAGE_PREFIX, ""))
            setModuleName(propertiesLoader.getProperty(Config.PROP_KEY_MODULE_NAME, ""))
            setCodeLoaction(propertiesLoader.getProperty(Config.PROP_KEY_CODE_LOACTION, ""))
            setAuthor(propertiesLoader.getProperty(Config.PROP_KEY_AUTHOR, SystemKit.getUser()))
            setVersion(propertiesLoader.getProperty(Config.PROP_KEY_VERSION, ""))
        }
    }

    private fun bindProperties() {
        with(config) {
            urlTextField.textProperty().bindBidirectional(dbUrlProperty())
            userTextField.textProperty().bindBidirectional(dbUserProperty())
            passwordField.textProperty().bindBidirectional(dbPasswordProperty())
            templateChoiceBox.selectionModelProperty().bindBidirectional(templateInfoProperty())
            moduleTextField.textProperty().bindBidirectional(moduleNameProperty())
            authorTextField.textProperty().bindBidirectional(authorProperty())
            versionTextField.textProperty().bindBidirectional(versionProperty())
            packagePrefixTextField.textProperty().bindBidirectional(packagePrefixProperty())
            locationTextField.textProperty().bindBidirectional(codeLoactionProperty())
            authorTextField.textProperty().bindBidirectional(authorProperty())
            versionTextField.textProperty().bindBidirectional(versionProperty())
        }
    }

    private fun initTempleComboBox() {
        val templatesPath = "${PathKit.getRuntimePath()}/../../../resources/main/templates/"
        val files = File(templatesPath).normalize().listFiles()
        val templateNameAndPaths = mutableListOf<Config.TemplateNameAndRootDir>()
        files.forEach {
            templateNameAndPaths.add(
                Config.TemplateNameAndRootDir(it.name, FilenameKit.normalize(it.absolutePath, true))
            )
        }
        templateChoiceBox.items = FXCollections.observableArrayList(*templateNameAndPaths.toTypedArray())
        templateChoiceBox.selectionModel = object : SingleSelectionModel<Config.TemplateNameAndRootDir>() {
            override fun getItemCount(): Int {
                return templateNameAndPaths.size
            }

            override fun getModelItem(index: Int): Config.TemplateNameAndRootDir {
                return templateNameAndPaths[index]
            }
        }
        templateChoiceBox.selectionModel.select(0)
    }

    fun canGoOn() {
        // test connection
        val dataSource = DataSourceKit.createDataSource(
            urlTextField.text.trim(),
            userTextField.text.trim(),
            passwordField.text
        )
        val context = KudosContext()
        context.addOtherInfos(Pair(KudosContext.OTHER_INFO_KEY_DATA_SOURCE, dataSource))
        KudosContextHolder.set(context)

        _testDbConnection()

        migrateDb(dataSource)

        // test template
        if (templateChoiceBox.selectionModel.isEmpty) {
            throw Exception("请选择模板方案！")
        }

        // package prefix
        if (packagePrefixTextField.text.isNullOrBlank()) {
            throw Exception("请填写包名前缀！")
        }

        // test module
        if (moduleTextField.text.isNullOrBlank()) {
            throw Exception("请填写模块名！")
        }

        // test location
        if (locationTextField.text.isNullOrBlank()) {
            throw Exception("代码生成目录不存在！")
        }

        // author location
        if (authorTextField.text.isNullOrBlank()) {
            throw Exception("请填写作者！")
        }

        // version location
        if (versionTextField.text.isNullOrBlank()) {
            throw Exception("请填写版本号！")
        }
    }

    /**
     * 执行脚本升级
     */
    private fun migrateDb(dataSource: DataSource) {
        val flywayProperties = FlywayProperties().apply {
            isBaselineOnMigrate = true
            baselineVersion = "0"
            encoding = Charsets.UTF_8
            isOutOfOrder = false
            isValidateOnMigrate = false
            isPlaceholderReplacement = false
        }
        FlywayKit.migrate("codegen", dataSource, flywayProperties)
    }

    private fun _testDbConnection() {
        try {
            RdbKit.newConnection(config.getDbUrl(), config.getDbUser(), config.getDbPassword()).use {
                RdbKit.testConnection(it)
            }
        } catch (_: Exception) {
            Alert(Alert.AlertType.ERROR, "连接失败！").show()
            throw Exception("数据库连接不上！")
        }
    }

    @FXML
    private fun testDbConnection() {
        _testDbConnection()
        Alert(Alert.AlertType.INFORMATION, "连接成功！").show()
    }

    @FXML
    private fun openFileChooser() {
        val directoryChooser = DirectoryChooser()
        val codeLoaction = config.getCodeLoaction()
        if (codeLoaction.isNotBlank()) {
            val file = File(codeLoaction)
            if (file.exists() && file.isDirectory) {
                directoryChooser.initialDirectory = file
            }
        }
        directoryChooser.title = "选择生成目录"
        val selectedFolder = directoryChooser.showDialog(openButton.scene.window)
        if (selectedFolder != null) {
            locationTextField.text = selectedFolder.absolutePath
        }
    }

    fun storeConfig() {
        val properties = propertiesLoader.properties
        with(properties) {
            setProperty(Config.PROP_KEY_DB_URL, config.getDbUrl())
            setProperty(Config.PROP_KEY_DB_USER, config.getDbUser())
            setProperty(Config.PROP_KEY_DB_PASSWORD, config.getDbPassword())
            setProperty(Config.PROP_KEY_TEMPLATE_NAME, config.getTemplateInfo().name)
            setProperty(Config.PROP_KEY_TEMPLATE_ROOT_DIR, config.getTemplateInfo().rootDir)
            setProperty(Config.PROP_KEY_PACKAGE_PREFIX, config.getPackagePrefix())
            setProperty(Config.PROP_KEY_MODULE_NAME, config.getModuleName())
            setProperty(Config.PROP_KEY_CODE_LOACTION, config.getCodeLoaction())
            setProperty(Config.PROP_KEY_MODULE_SUGGESTIONS, moduleSuggestions?.joinToString())
            setProperty(Config.PROP_KEY_AUTHOR, config.getAuthor())
            setProperty(Config.PROP_KEY_VERSION, config.getVersion())
        }
        try {
            FileOutputStream(propertiesFile).use { os -> properties.store(os, null) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private val properties: Properties
        get() {
            val properties = Properties()
            if (!propertiesFile.exists()) { // 第一次使用，预设组件默认值
                val parentFile = propertiesFile.parentFile
                if (!parentFile.exists()) {
                    if (!parentFile.mkdir()) {
                        throw Exception(parentFile.toString() + "目录创建失败！")
                    }
                }
                with(properties) {
                    setProperty(Config.PROP_KEY_DB_URL, "jdbc:h2:tcp://localhost:9092/./h2;DATABASE_TO_UPPER=false")
                    setProperty(Config.PROP_KEY_DB_USER, "sa")
                    setProperty(Config.PROP_KEY_DB_PASSWORD, "")
                    setProperty(Config.PROP_KEY_PACKAGE_PREFIX, "")
                    setProperty(Config.PROP_KEY_MODULE_NAME, "")
                    setProperty(Config.PROP_KEY_CODE_LOACTION, userHome)
                    setProperty(Config.PROP_KEY_MODULE_SUGGESTIONS, "")
                    setProperty(Config.PROP_KEY_AUTHOR, SystemKit.getUser())
                    setProperty(Config.PROP_KEY_VERSION, "1.0.0")
                }
            } else {
                try {
                    FileInputStream(propertiesFile).use { `is` -> properties.load(`is`) }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return properties
        }

}