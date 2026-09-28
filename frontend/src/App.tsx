import { useEffect, useState } from 'react'
import api from './services/api'
import './index.css'

function App() {
  const [pingStatus, setPingStatus] = useState<string>('Pinging backend...');

  useEffect(() => {
    api.get('/ping')
      .then(response => {
        if (response.data.success) {
          setPingStatus('Backend is UP: ' + response.data.message);
        } else {
          setPingStatus('Backend responded with unexpected format.');
        }
      })
      .catch(error => {
        setPingStatus('Backend is DOWN or unreachable: ' + error.message);
      });
  }, []);

  return (
    <div className="min-h-screen bg-slate-900 text-white flex flex-col items-center justify-center p-4">
      <h1 className="text-4xl font-bold mb-4">Fake Company Detector</h1>
      <p className="text-xl mb-8">Phase 1: Project Setup</p>
      
      <div className="bg-slate-800 p-6 rounded-lg shadow-lg border border-slate-700 w-full max-w-md">
        <h2 className="text-lg font-semibold mb-2">Backend Connection Status:</h2>
        <div className={`p-4 rounded ${pingStatus.includes('UP') ? 'bg-green-900/50 text-green-400 border border-green-800' : 'bg-red-900/50 text-red-400 border border-red-800'}`}>
          {pingStatus}
        </div>
      </div>
    </div>
  )
}

export default App
