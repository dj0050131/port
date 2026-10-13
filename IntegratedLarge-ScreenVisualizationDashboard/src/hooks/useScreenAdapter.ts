import { onMounted, onUnmounted, ref } from 'vue'

const BASE_WIDTH = 1920
const BASE_HEIGHT = 1080

type ScreenStyle = {
  width: string
  height: string
  transform: string
}

export const useScreenAdapter = () => {
  const screenStyle = ref<ScreenStyle>({
    width: `${BASE_WIDTH}px`,
    height: `${BASE_HEIGHT}px`,
    transform: 'translate(0, 0) scale(1)',
  })

  const updateScale = () => {
    const { innerWidth, innerHeight } = window
    const widthScale = innerWidth / BASE_WIDTH
    const heightScale = innerHeight / BASE_HEIGHT
    const scale = Math.min(widthScale, heightScale)

    screenStyle.value = {
      width: `${BASE_WIDTH}px`,
      height: `${BASE_HEIGHT}px`,
      transform: `translate(0, 0) scale(${scale})`,
    }
  }

  // })
  //
  // })

  return {
    screenStyle,
  }
}

