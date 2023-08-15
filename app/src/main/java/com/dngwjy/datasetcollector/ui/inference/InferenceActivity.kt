package com.dngwjy.datasetcollector.ui.inference

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dngwjy.datasetcollector.databinding.ActivityInferenceBinding
import com.dngwjy.datasetcollector.toast

class InferenceActivity : AppCompatActivity(),InferenceView {
    private lateinit var binding:ActivityInferenceBinding
    private lateinit var presenter: InferencePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityInferenceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        presenter=InferencePresenter(this)

        with(binding){
            btnStart.setOnClickListener {
                if(etSettingIp.text.isNullOrBlank()){
                    toast("Server URL must defined first!")
                }else{
                    startScanningInference()
                }
            }
        }
    }

    private fun startScanningInference(){

    }

    override fun onLoading() {

    }

    override fun onSuccessSendData() {

    }

    override fun onErrorSendData() {

    }
}