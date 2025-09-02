import {View, Text, Image} from '@tarojs/components'
import {AtButton} from 'taro-ui'//带大括号的是命名导出
import './index.scss'
// eslint-disable-next-line import/first
import GlobalFooter from '../../components/GlobalFooter'
import headerBg from '../../assets/headerBg.jpg'
import questionResults from '../../data/question_results.json'
import questions from '../../data/questions.json';
// @ts-ignore
import {getBestQuestionResult} from '../../utils/bizUtils';

// eslint-disable-next-line import/first
import Taro from "@tarojs/taro";

/**
 *  主页
 * @constructor
 */
export default function Index() {

  const answerList = Taro.getStorageSync('answerList');

  console.log('answerList:', answerList);
  if(!answerList || answerList.length < 1){
    Taro.showToast({
      title: '答案为空',
      icon: 'error',
      duration: 2000
    })
  }

  const results = getBestQuestionResult(answerList,questions,questionResults);

  return (
    <View className='resultPage'>
      <View className='intro-box'>
        <Text className='at-article__h1 headline'>{results.resultName}</Text>
        <Text className='at-article__h2 subtext'>{results.resultDesc}</Text>
        <AtButton type='primary' className='start-btn' onClick={() => {
          Taro.reLaunch({url: '/pages/index/index'})
        }}
        > 返回主页</AtButton>
        <Image className='bg'
          src={headerBg} style={{width: "100%"}} mode="aspectFill"
        />
      </View>


      <GlobalFooter />
    </View>


  )
}
