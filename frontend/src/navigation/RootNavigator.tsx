import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { BottomTabNav } from './BottomTabNav';
import ChatLearnScreen from '../screens/ChatLearnScreen';
import LearningResultScreen from '../screens/LearningResultScreen';
import { SplashScreen } from '../screens/auth/SplashScreen';
import { LoginScreen } from '../screens/auth/LoginScreen';
import { SignUpScreen } from '../screens/auth/SignUpScreen';
import { EmailVerifyScreen } from '../screens/auth/EmailVerifyScreen';
import { OnboardingScreen } from '../screens/auth/OnboardingScreen';

const Stack = createNativeStackNavigator();

export const RootNavigator = () => {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      {/* Auth Flow */}
      <Stack.Screen name="Splash" component={SplashScreen} />
      <Stack.Screen name="Login" component={LoginScreen} />
      <Stack.Screen name="SignUp" component={SignUpScreen} />
      <Stack.Screen name="EmailVerify" component={EmailVerifyScreen} />
      <Stack.Screen name="Onboarding" component={OnboardingScreen} />

      {/* Main Tab Flow (하단 탭이 있는 화면들) */}
      <Stack.Screen name="MainTab" component={BottomTabNav} />

      {/* Learning Flow (하단 탭이 없는 화면들) */}
      <Stack.Screen name="ChatLearn" component={ChatLearnScreen} />
      <Stack.Screen name="LearningResult" component={LearningResultScreen} />
    </Stack.Navigator>
  );
};
