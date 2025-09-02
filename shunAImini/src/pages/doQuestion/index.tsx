import {Text, View, Image} from '@tarojs/components'

import './index.scss'
// eslint-disable-next-line import/first
import GlobalFooter from '../../components/GlobalFooter'
import {AtButton, AtCurtain, AtRadio} from "taro-ui";
import questions from '../../data/questions.json';
import mao from '../../assets/tan.jpg';
// eslint-disable-next-line import/first
import {useState} from "react";
import Taro from "@tarojs/taro";

/**
 * 答题页面
 */
export default function Index() {

  const [index, setIndex] = useState(0);//题号状态
  const [isOpened, setIsOpened] = useState(false);//幕帘状态
  const [answerList, setAnswerlist] = useState<String[]>([]);//回答列表状态
  const question = questions[index];
  const currentAnswer = answerList[index] || "";//从anserList中提取答案，如果没有，就给他个空字符串
  //这里直接使用索引直接计算question，这样更加简便
  //如果再加一个状态如 const[currentQuestion,setCurrentQuestion] = useState(questions[0])
  //然后再使用useEffect去修改currentQuestion
  //这样的方法更加的React，给了question也可以作为状态的能力
  //用 useEffect + 额外状态 的写法适合更复杂的场景，比如：
  //题目内容需要异步获取或额外加工；
  //题目会在用户交互中被动态修改；
  //你需要对 currentQuestion 独立操作（而不直接通过索引控制）。
  const questionOptions = question.options.map(option => (
    {label: `${option.key}.${option.value}`, value: option.key}
  ))


  // "options": [
  //       {
  //         "result": "I",
  //         "value": "独自工作",
  //         "key": "A"
  //       },
  //       {
  //         "result": "E",
  //         "value": "与他人合作",
  //         "key": "B"
  //       }
  //     ],


  //"questionOptions":[
  //          {
  //           "label":A.独自工作,
  //           "value":A
  //          }
  //          ......
  //
  // ]


  //const 表示这个变量绑定（引用）不能变，但对象内容可以变。
  //不可重新赋值，但是给他赋值的那个对象可以修改内容

  //useState用来声明状态变量，且在修改状态变量的时候刷新页面（局部刷新）
  //useState(0)返回一个数组，有两个元素类型[当前状态的值（0）,更新状态的函数]
  //通常的写法：数组解构赋值
  //const [index,setIndex] = useState(0);
  //这样就把useState(0)生成的东西分别赋给了index和setIndex

  //`${option.key}.${option.value}`这里是模板字符串，用于拼接字符串，是“+”的代替
  //等同于 option.key+'.'+option.value
  //.map()是数组方法，遍历数组并生成一个新的数组
  //option=>{}是箭头函数，和lambda表达式一样，option是函数参数，{}里返回值，如果直接返回一个对象，可以变成
  //option=>({})  ()的作用是告诉函数返回的值就是个对象，而不会误以为{}是函数体

  // @ts-ignore
  return (
    <View className='questionPage'>
      <Text className='at-article__h1 headline'>{index + 1}.{question.title}</Text>


      <AtRadio options={questionOptions} value={currentAnswer} onClick={(val) => {
        const newList = [...answerList];//浅拷贝，把answerList的值拷贝给newList
        newList[index] = val;
        setAnswerlist(newList);
      }}
      />
      {/*options里的value在选中某个选项后就会被自动传给onClick的val，并且通过setSelectedOption来令selectedOption=val*/}
      {/*最后value={selectedOption}令value等于val*/}
      {/*JS变量必须使用{}    这里的questionOptions就是 */}

      {/*
      渲染条件
      1.index===0  第一题   下一题
      2.index>0&index<0 第二题至倒数第二题 上一题 下一题
      3.index===(questions.length-1)  最后一题  查看结果

    {index > 0 && <AtButton />} 短路与逻辑运算
    可认为是三元表达式的简便写法，等价于：
    {index > 0 ? <AtButton /> : null}

    多个JSX组件要使用<></>包裹起来，再用()包裹起来
    这是因为JS的语法解析机制，JS会在return和<之间加上分号，导致没有东西返回， 如果使用()包裹，就不会加分号
      */}
      {index === 0 && <AtButton type='primary' className='start-btn' disabled={!currentAnswer} onClick={() => {
        setIndex(index + 1)
      }}
      >下一题</AtButton>}

      {index > 0 && index < questions.length - 1 && (
        <>
          <AtButton type='primary' className='start-btn'  disabled={!currentAnswer} onClick={() => {
            setIndex(index + 1)
          }}
          >下一题</AtButton>
          <AtButton className='start-btn' onClick={() => {
            setIndex(index - 1)
          }}
          >上一题</AtButton>
        </>
      )}
      {index === questions.length - 1 && (
        <>
          <AtButton type='primary' className='start-btn' disabled={!currentAnswer} onClick={() => {

            setIsOpened(true);
            Taro.setStorageSync('answerList', answerList);
            Taro.navigateTo({
              url: '/pages/result/index',
            });

          }}
          >查看结果</AtButton>
          <AtButton className='start-btn' onClick={() => {
            setIndex(index - 1)
          }}
          >上一题</AtButton>
        </>
      )}


      <AtCurtain
        isOpened={isOpened}
        onClose={() => {
          setIsOpened(false)
        }}
      >
        <Image
          style='width:100%;height:250px'
          src={mao}
        />
      </AtCurtain>
      <GlobalFooter />
    </View>


  )
}
