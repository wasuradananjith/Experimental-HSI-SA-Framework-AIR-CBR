import json
import time
import numpy as np
import io
from PIL import Image, ImageOps

from zmqRemoteApi import RemoteAPIClient

parentScriptHandle = None

def main():
    return "hello world from python"

def connect():
    print('Program started...')
    client = RemoteAPIClient(host='192.168.50.133', port=23000)
    #client = RemoteAPIClient(host='localhost', port=23000)
    # ngrok tcp 23000
    #client = RemoteAPIClient(host='0.tcp.au.ngrok.io', port=16945)
    print('Program connected...')
    sim = client.getObject('sim')
    print('Sim retrieved...')
    return sim

def isSimStopped(sim):
    return sim.getSimulationState() == sim.simulation_stopped;

def getCuboidsLocations(sim):
    parentScriptHandle = sim.getScript(0)
    return json.dumps(sim.callScriptFunction("getCuboidsLocations", parentScriptHandle))

def getRectangularStaticObstacles(sim):
    parentScriptHandle = sim.getScript(0)
    return sim.callScriptFunction("getRectangularStaticObstacles", parentScriptHandle)

def createCylinderRegion(sim, regionId, centerX, centerY, radius):
    parentScriptHandle = sim.getScript(0)
    return sim.callScriptFunction("createCylinderRegion", parentScriptHandle, regionId, centerX, centerY, radius)

def updateCylinderRadius(sim, regionId, radius):
    parentScriptHandle = sim.getScript(0)
    return sim.callScriptFunction("updateCylinderRadius", parentScriptHandle, regionId, radius)

def deleteCylinderRegion(sim, regionId):
    parentScriptHandle = sim.getScript(0)
    return sim.callScriptFunction("deleteCylinderRegion", parentScriptHandle, regionId)

def createDangerousRegion(sim, regionId, centerX, centerY, radius):
    parentScriptHandle = sim.getScript(0)
    return sim.callScriptFunction("createDangerousRegion", parentScriptHandle, regionId, centerX, centerY, radius)

def deleteDangerousRegion(sim, regionId):
    parentScriptHandle = sim.getScript(0)
    return sim.callScriptFunction("deleteDangerousRegion", parentScriptHandle, regionId)

def updateDangerousRegionRadius(sim, regionId, radius):
    parentScriptHandle = sim.getScript(0)
    return sim.callScriptFunction("updateDangerousRegionRadius", parentScriptHandle, regionId, radius)

def createAttractiveRegion(sim, regionId, centerX, centerY, radius):
    parentScriptHandle = sim.getScript(0)
    return sim.callScriptFunction("createAttractiveRegion", parentScriptHandle, regionId, centerX, centerY, radius)

def deleteAttractiveRegion(sim, regionId):
    parentScriptHandle = sim.getScript(0)
    return sim.callScriptFunction("deleteAttractiveRegion", parentScriptHandle, regionId)

def updateAttractiveRegionRadius(sim, regionId, radius):
    parentScriptHandle = sim.getScript(0)
    return sim.callScriptFunction("updateAttractiveRegionRadius", parentScriptHandle, regionId, radius)

def getTargetRegion(sim):
    parentScriptHandle = sim.getScript(0)
    return sim.callScriptFunction("getTargetRegion", parentScriptHandle)

def createBreadcrumb(sim, breadcrumbId, xCord, yCord):
    parentScriptHandle = sim.getScript(0)
    return sim.callScriptFunction("createBreadcrumb", parentScriptHandle, breadcrumbId, xCord, yCord)

def deleteBreadcrumb(sim, breadcrumbId):
    parentScriptHandle = sim.getScript(0)
    return sim.callScriptFunction("deleteBreadcrumb", parentScriptHandle, breadcrumbId)

def stopSim(sim):
    sim.stopSimulation()
    
def stopRobot(sim):
    robot = sim.getObject('./PioneerP3DX[1]')
    scriptHandle = sim.getScript(1,robot)
    
    parameter = sim.getScriptStringParam(scriptHandle,sim.scriptstringparam_text)
    sim.setScriptInt32Param(scriptHandle,sim.scriptintparam_enabled,0)
    
    motorLeft = sim.getObject("/PioneerP3DX[1]/leftMotor")
    sim.setJointTargetVelocity(motorLeft,0)
    
    motorRight = sim.getObject("/PioneerP3DX[1]/rightMotor")
    sim.setJointTargetVelocity(motorRight,0)
    
    return "Robot stopped"

def startRobot(sim):
    robot = sim.getObject('./PioneerP3DX[1]')
    scriptHandle = sim.getScript(1,robot)
    
    parameter=sim.getScriptStringParam(scriptHandle,sim.scriptstringparam_text)
    sim.setScriptInt32Param(scriptHandle,sim.scriptintparam_enabled,1)
    return "Robot started"
    
def turnRight(sim):
    stopRobot(sim)
    motorLeft = sim.getObject("/PioneerP3DX[1]/leftMotor")
    sim.setJointTargetVelocity(motorLeft,0.3)
    return "Robot turned right"
    
def turnLeft(sim):
    stopRobot(sim)
    motorRight = sim.getObject("/PioneerP3DX[1]/rightMotor")
    sim.setJointTargetVelocity(motorRight,0.3)
    return "Robot turned left"

def getVisionSensorImage(sim):
    sensorHandle = sim.getObject("/Vision_sensor")
    image, resolution = sim.getVisionSensorImg(sensorHandle)
    imageTable = sim.unpackUInt8Table(image)
    imageArray = np.array(imageTable, dtype=np.uint8)    
    imageArray.resize([resolution[0], resolution[1], 3])
    pilImage = Image.fromarray( np.rot90(imageArray, 2))
    pilImage = ImageOps.mirror(pilImage)
    
    buff = io.BytesIO()
    pilImage.save(buff, format="PNG")
    return buff.getvalue()
    #imageString = base64.b64decode(buff.getvalue())
    #return ""+str(imageString, 'utf-8')
    
    #return imageArray
    
    #f = open("C:/Users/z5415828/OneDrive - UNSW/Documents/AndroidStudioProjects/MyApplication/app/src/main/python/demofile2.txt", "w")
    #f.write(str(imageTable))
    #f.close()
    #print(len(imageTable))
    
    #return imageArray
    #mlp.imshow(imageArray, origin="lower")

def stopCuboid(sim):
    cuboid = sim.getObject('./CuboidRobot[0]')
    scriptHandle = sim.getScript(1,cuboid)
    sim.setScriptInt32Param(scriptHandle,sim.scriptintparam_enabled,0)
    return "Cuboid stopped"

def startCuboid(sim):
    cuboid = sim.getObject('./CuboidRobot[0]')
    scriptHandle = sim.getScript(1,cuboid)
    sim.setScriptInt32Param(scriptHandle,sim.scriptintparam_enabled,1)
    return "Cuboid started"

def turnRightCuboid(sim):
    stopCuboid(sim)
    cuboid = sim.getObject('./CuboidRobot[0]')
    position = sim.getObjectPosition(cuboid, sim.handle_world)
    position[0] += 0.025
    sim.setObjectPosition(cuboid, sim.handle_world, position)
    return "Cuboid navigated right"

def turnLeftCuboid(sim):
    stopCuboid(sim)
    cuboid = sim.getObject('./CuboidRobot[0]')
    position = sim.getObjectPosition(cuboid, sim.handle_world)
    position[0] -= 0.025
    sim.setObjectPosition(cuboid, sim.handle_world, position)
    return "Robot navigated left"

def x():
    print('Program started')
    client = RemoteAPIClient()
    sim = client.getObject('sim')
    
    # When simulation is not running, ZMQ message handling could be a bit
    # slow, since the idle loop runs at 8 Hz by default. So let's make
    # sure that the idle loop runs at full speed for this program:
    defaultIdleFps = sim.getInt32Param(sim.intparam_idle_fps)
    sim.setInt32Param(sim.intparam_idle_fps, 0)
    
    # Create a few dummies and set their positions:
    handles = [sim.createDummy(0.01, 12 * [0]) for _ in range(50)]
    for i, h in enumerate(handles):
        sim.setObjectPosition(h, -1, [0.01 * i, 0.01 * i, 0.01 * i])
    
    # Run a simulation in asynchronous mode:
    sim.startSimulation()
    while (t := sim.getSimulationTime()) < 3:
        s = f'Simulation time: {t:.2f} [s] (simulation running asynchronously '\
            'to client, i.e. non-stepped)'
        print(s)
        sim.addLog(sim.verbosity_scriptinfos, s)
    sim.stopSimulation()
    # If you need to make sure we really stopped:
    while sim.getSimulationState() != sim.simulation_stopped:
        time.sleep(0.1)
    
    # Run a simulation in stepping mode:
    client.setStepping(True)
    sim.startSimulation()
    while (t := sim.getSimulationTime()) < 3:
        s = f'Simulation time: {t:.2f} [s] (simulation running synchronously '\
            'to client, i.e. stepped)'
        print(s)
        sim.addLog(sim.verbosity_scriptinfos, s)
        client.step()  # triggers next simulation step
    sim.stopSimulation()
    
    # Remove the dummies created earlier:
    for h in handles:
        sim.removeObject(h)
    
    # Restore the original idle loop frequency:
    sim.setInt32Param(sim.intparam_idle_fps, defaultIdleFps)
    
    print('Program ended')
    
    return "program ended"
