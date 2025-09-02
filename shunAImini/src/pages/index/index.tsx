import { View, Text, Image} from '@tarojs/components'
import { AtButton} from 'taro-ui'//带大括号的是命名导出
import './index.scss'
// eslint-disable-next-line import/first
import GlobalFooter from '../../components/GlobalFooter'
import headerBg from '../../assets/headerBg.jpg'
import Taro from "@tarojs/taro";
/**
 *  主页
 * @constructor
 */
export default function Index() {

  // @ts-ignore
  return (
      <View className='indexPage'>
        <View className='intro-box'>
          <Text className='at-article__h1 headline'>MBTI 性格测试</Text>
          <Text className='at-article__h2 subtext'>也许你一直在寻找一个答案——我是谁？我为何如此？</Text>
          <Text className='at-article__h2 subtext'>只需 5 分钟，一份专属性格分析将揭示你内在的真实自己，帮助你看清行为背后的动因，洞察与他人的相处之道。</Text>
          <AtButton
            type='primary'
            className='start-btn'
            onClick={()=>{Taro.navigateTo({
              url: '/pages/doQuestion/index',
            })}
          }

          //app.ts / app.json 中的 pages 是 相对路径，不加 / 是规范写法。'pages/index/index',
          //
          // Taro.navigateTo({ url }) 中的 url 是 绝对路径，加 / 是规范写法。 '/pages/doQuestion/index'
          >开启测试，认识更真实的自己。</AtButton>

          <Image className='bg'
            src={headerBg} style={{ width: "100%" }} mode="aspectFill"
          />
        </View>





        <GlobalFooter />
      </View>


    )
}
