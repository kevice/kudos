package io.kudos.base.bean.validation.constraint.validator

import io.kudos.base.bean.validation.constraint.annotations.DictCode
import io.kudos.base.bean.validation.teminal.convert.converter.IDictCodeFinder
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.util.*

class DictCodeValidator : ConstraintValidator<DictCode, CharSequence?> {
    private lateinit var dictCode: DictCode

    override fun initialize(dictCode: DictCode) {
        this.dictCode = dictCode
    }

    override fun isValid(value: CharSequence?, constraintValidatorContext: ConstraintValidatorContext): Boolean {
        if (value.isNullOrBlank()) {
            return true
        }
        val dictMap: MutableMap<*, *> = dictCodeConvertor(dictCode.module, dictCode.dictType)
        return dictMap.containsKey(value)
    }

    private fun dictCodeConvertor(module: String?, dictType: String?): MutableMap<String?, String?> {
        val dictCodeFinders = ServiceLoader.load(IDictCodeFinder::class.java)
        for (dictCodeFinder in dictCodeFinders) {
            return dictCodeFinder.getDictData(module, dictType)
        }
        return HashMap<String?, String?>()
    }

}
