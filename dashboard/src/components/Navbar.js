import React from 'react';
import icon from '../assets/images/icon.png';

export default function Navbar() {
    return (
        <nav className="bg-green-800">
            <div className="px-2 sm:px-6 lg:px-8">
                <div className="relative flex h-16 items-center">
                    <div className="flex-1 flex items-center justify-center sm:items-stretch sm:justify-start">
                        <div className="flex-shrink-0 flex items-center">
                            <img className="h-10" src={icon} alt='Icon' width={"100%"}/>
                            <span className="self-center text-2xl font-semibold whitespace-nowrap text-white dark:text-white ml-2">CEP4Pollution</span>
                        </div>
                        
                    </div>
                </div>
            </div>
        </nav>
    );
}
